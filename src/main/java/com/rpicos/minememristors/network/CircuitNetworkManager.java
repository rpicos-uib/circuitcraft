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
 * the same electrical node if each presents a conductive face toward the other. Wires are
 * conductive on all six faces; components only on their two lead faces (their facing axis) - so a
 * component's insulated sides never short its two terminals together.
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
				return;
			}
			for (NetworkBlockEntity entity : participants.values()) {
				if (entity instanceof ComponentBlockEntity component) {
					component.recordSample();
				}
			}
		}
	}

	private void rebuild() {
		dirty = false;
		circuit = new Circuit();

		Map<BlockPos, BlockPos> parent = new HashMap<>();
		for (BlockPos p : participants.keySet()) {
			parent.put(p, p);
		}

		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			for (Direction direction : Direction.values()) {
				if (!entity.isConductiveTowards(direction)) continue;
				BlockPos neighborPos = pos.relative(direction);
				NetworkBlockEntity neighbor = participants.get(neighborPos);
				if (neighbor != null && neighbor.isConductiveTowards(direction.getOpposite())) {
					union(parent, pos, neighborPos);
				}
			}
		}

		Map<BlockPos, Integer> nodeIdByRoot = new HashMap<>();
		Map<BlockPos, Integer> nodeIdByPos = new HashMap<>();
		for (BlockPos p : participants.keySet()) {
			BlockPos root = find(parent, p);
			int nodeId = nodeIdByRoot.computeIfAbsent(root, r -> circuit.addNode());
			nodeIdByPos.put(p, nodeId);
		}

		for (NetworkBlockEntity entity : participants.values()) {
			if (entity instanceof ComponentBlockEntity component) {
				int nodeA = nodeFor(component.terminalA(), nodeIdByPos, circuit);
				int nodeB = nodeFor(component.terminalB(), nodeIdByPos, circuit);
				component.addToCircuit(circuit, nodeA, nodeB);
			}
		}
	}

	private static int nodeFor(BlockPos terminalPos, Map<BlockPos, Integer> nodeIdByPos, Circuit circuit) {
		Integer existing = nodeIdByPos.get(terminalPos);
		// An unwired terminal gets its own floating node - harmless thanks to the solver's
		// ground-leak conductance, and lets a half-built circuit still simulate.
		return existing != null ? existing : circuit.addNode();
	}

	private static BlockPos find(Map<BlockPos, BlockPos> parent, BlockPos p) {
		BlockPos root = p;
		while (!parent.get(root).equals(root)) {
			root = parent.get(root);
		}
		BlockPos cur = p;
		while (!cur.equals(root)) {
			BlockPos next = parent.get(cur);
			parent.put(cur, root);
			cur = next;
		}
		return root;
	}

	private static void union(Map<BlockPos, BlockPos> parent, BlockPos a, BlockPos b) {
		BlockPos rootA = find(parent, a);
		BlockPos rootB = find(parent, b);
		if (!rootA.equals(rootB)) {
			parent.put(rootA, rootB);
		}
	}
}
