package com.rpicos.minememristors.network;

import com.rpicos.minememristors.MineMemristors;
import com.rpicos.minememristors.blockentity.ComponentBlockEntity;
import com.rpicos.minememristors.blockentity.NetworkBlockEntity;
import com.rpicos.minememristors.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * One instance per loaded ServerLevel. Tracks every placed wire/component position and, once per
 * tick, rebuilds the circuit topology (only when something changed) and steps the simulation.
 *
 * <p>Topology is a union-find over "conductive" adjacency: two neighbouring positions merge into
 * the same electrical node if each presents a conductive face toward the other. Wire has a single
 * graph identity per block (its whole body is one electrical point, conductive on all six faces).
 * A component instead gets <b>two separate</b> graph identities - one per lead - keyed by
 * {@code (pos, side)} rather than by its bare position. Keying both leads by the same block
 * position would let the component's own body union-find its way into being a single node,
 * short-circuiting the very element it's supposed to be wired across.
 */
public class CircuitNetworkManager {
	private static final Map<ServerLevel, CircuitNetworkManager> INSTANCES = new WeakHashMap<>();
	private static final double TICK_SECONDS = 1.0 / 20.0;

	public static CircuitNetworkManager forLevel(ServerLevel level) {
		return INSTANCES.computeIfAbsent(level, l -> new CircuitNetworkManager());
	}

	private final Map<BlockPos, NetworkBlockEntity> participants = new HashMap<>();
	private boolean dirty = true;
	private Circuit circuit;
	// Set when the last step() threw (e.g. a voltage source shorted across itself); stepping is
	// skipped until the topology changes again, so one bad wiring doesn't spam the log every tick
	// or take down the server thread.
	private boolean faulted;
	// Per-component (nodeA, nodeB) from the most recent rebuild(), kept only so a solver fault can
	// be logged with enough detail (which block, which facing, which nodes) to diagnose from the
	// server log alone - no need to reconstruct the wiring from a screenshot.
	private final Map<BlockPos, int[]> lastComponentNodes = new HashMap<>();

	public void register(BlockPos pos, NetworkBlockEntity entity) {
		participants.put(pos.immutable(), entity);
		dirty = true;
	}

	public void unregister(BlockPos pos) {
		if (participants.remove(pos) != null) {
			dirty = true;
		}
	}

	public void markDirty() {
		dirty = true;
	}

	public void tick(ServerLevel level) {
		// Self-heals entries left behind when a chunk unloads without setRemoved() firing.
		participants.keySet().removeIf(pos -> !level.isLoaded(pos));

		if (dirty) {
			rebuild();
			faulted = false;
		}
		if (circuit != null && !faulted) {
			try {
				circuit.step(TICK_SECONDS);
			} catch (ArithmeticException e) {
				faulted = true;
				MineMemristors.LOGGER.warn(
						"Circuit network paused after solver error (will resume once its wiring changes): {}",
						e.getMessage());
				logComponentNodesForDiagnosis();
				return;
			}
			for (NetworkBlockEntity entity : participants.values()) {
				if (entity instanceof ComponentBlockEntity component) {
					component.recordSample();
				}
			}
		}
	}

	private void logComponentNodesForDiagnosis() {
		for (Map.Entry<BlockPos, int[]> entry : lastComponentNodes.entrySet()) {
			BlockPos pos = entry.getKey();
			int[] nodes = entry.getValue();
			NetworkBlockEntity entity = participants.get(pos);
			String facing = entity instanceof ComponentBlockEntity component
					? component.getFacing().toString()
					: "?";
			MineMemristors.LOGGER.warn("  {} at {} facing={}: node A={}, node B={}{}",
					entity == null ? "?" : entity.getClass().getSimpleName(), pos, facing,
					nodes[0], nodes[1], nodes[0] == nodes[1] ? "  <-- both terminals on the same node (shorted)" : "");
		}
	}

	/** A wire's graph identity is just its position (one electrical point for the whole block).
	 *  A component's graph identity is one of these per lead, so its two terminals can never be
	 *  merged into each other via the component's own body. */
	private record NodeKey(BlockPos pos, Direction side) {
	}

	private static Object keyFor(BlockPos pos, NetworkBlockEntity entity, Direction side) {
		return entity instanceof ComponentBlockEntity ? new NodeKey(pos, side) : pos;
	}

	private void rebuild() {
		dirty = false;
		circuit = new Circuit();
		lastComponentNodes.clear();

		Map<Object, Object> parent = new HashMap<>();
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			if (entry.getValue() instanceof ComponentBlockEntity component) {
				Object a = new NodeKey(pos, component.getFacing());
				Object b = new NodeKey(pos, component.getFacing().getOpposite());
				parent.put(a, a);
				parent.put(b, b);
			} else {
				parent.put(pos, pos);
			}
		}

		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			for (Direction direction : Direction.values()) {
				if (!entity.isConductiveTowards(direction)) continue;
				BlockPos neighborPos = pos.relative(direction);
				NetworkBlockEntity neighbor = participants.get(neighborPos);
				if (neighbor != null && neighbor.isConductiveTowards(direction.getOpposite())) {
					Object myKey = keyFor(pos, entity, direction);
					Object neighborKey = keyFor(neighborPos, neighbor, direction.getOpposite());
					union(parent, myKey, neighborKey);
				}
			}
		}

		Map<Object, Integer> nodeIdByRoot = new HashMap<>();
		Map<Object, Integer> nodeIdByKey = new HashMap<>();
		for (Object key : parent.keySet()) {
			Object root = find(parent, key);
			int nodeId = nodeIdByRoot.computeIfAbsent(root, r -> circuit.addNode());
			nodeIdByKey.put(key, nodeId);
		}

		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			if (entry.getValue() instanceof ComponentBlockEntity component) {
				BlockPos pos = entry.getKey();
				int nodeA = nodeIdByKey.get(new NodeKey(pos, component.getFacing()));
				int nodeB = nodeIdByKey.get(new NodeKey(pos, component.getFacing().getOpposite()));
				component.addToCircuit(circuit, nodeA, nodeB);
				lastComponentNodes.put(pos, new int[] {nodeA, nodeB});
			}
		}
	}

	private static Object find(Map<Object, Object> parent, Object key) {
		Object root = key;
		while (!parent.get(root).equals(root)) {
			root = parent.get(root);
		}
		Object cur = key;
		while (!cur.equals(root)) {
			Object next = parent.get(cur);
			parent.put(cur, root);
			cur = next;
		}
		return root;
	}

	private static void union(Map<Object, Object> parent, Object a, Object b) {
		Object rootA = find(parent, a);
		Object rootB = find(parent, b);
		if (!rootA.equals(rootB)) {
			parent.put(rootA, rootB);
		}
	}
}
