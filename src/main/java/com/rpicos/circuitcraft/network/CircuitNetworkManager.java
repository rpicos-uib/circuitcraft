package com.rpicos.circuitcraft.network;

import com.rpicos.circuitcraft.CircuitCraft;
import com.rpicos.circuitcraft.blockentity.AcSourceBlockEntity;
import com.rpicos.circuitcraft.blockentity.AcStampable;
import com.rpicos.circuitcraft.blockentity.AmmeterBlockEntity;
import com.rpicos.circuitcraft.blockentity.ComponentBlockEntity;
import com.rpicos.circuitcraft.blockentity.GroundBlockEntity;
import com.rpicos.circuitcraft.blockentity.NetworkBlockEntity;
import com.rpicos.circuitcraft.blockentity.OpAmpBlockEntity;
import com.rpicos.circuitcraft.blockentity.Probeable;
import com.rpicos.circuitcraft.blockentity.SingleNodeBlockEntity;
import com.rpicos.circuitcraft.sim.AcCircuit;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Complex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * One instance per loaded ServerLevel. Tracks every placed wire/component position and, once per
 * tick, rebuilds the circuit topology (only when something changed) and steps the simulation.
 *
 * <p>Topology is a union-find over "conductive" adjacency: two neighbouring positions merge into
 * the same electrical node if each presents a conductive face toward the other. A
 * {@link SingleNodeBlockEntity} (wire, ground) has a single graph identity per block - its whole
 * body is one electrical point, conductive on all six faces. Anything else (a two-terminal
 * component, or a three-terminal op-amp) instead gets one separate graph identity per lead,
 * keyed by {@code (pos, side)} rather than by its bare position: keying every lead by the same
 * block position would let the block's own body union-find its way into a single node,
 * short-circuiting the very element it's supposed to be wired across. This generalizes to any
 * number of terminals for free - {@link #keyFor} only needs to know whether an entity is a
 * single shared point or not, since the specific conductive {@code direction} that triggered a
 * given union already identifies exactly which lead is involved.
 *
 * <p>The same topology (see {@link #computeNodeAssignment}) is reused, unchanged, by the AC
 * (Bode-plot) sweep in {@link #computeAcSweep}: that method just builds a separate,
 * complex-valued {@link AcCircuit} on top of the identical node numbering rather than the
 * transient {@link Circuit} the regular per-tick simulation uses.
 */
public class CircuitNetworkManager {
	private static final Map<ServerLevel, CircuitNetworkManager> INSTANCES = new WeakHashMap<>();
	private static final double TICK_SECONDS = 1.0 / 20.0;
	private static final int AC_SWEEP_POINTS = 60;

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
				CircuitCraft.LOGGER.warn(
						"Circuit network paused after solver error (will resume once its wiring changes): {}",
						e.getMessage());
				logComponentNodesForDiagnosis();
				return;
			}
			for (NetworkBlockEntity entity : participants.values()) {
				if (entity instanceof Probeable probeable) {
					probeable.recordSample();
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
			CircuitCraft.LOGGER.warn("  {} at {} facing={}: node A={}, node B={}{}",
					entity == null ? "?" : entity.getClass().getSimpleName(), pos, facing,
					nodes[0], nodes[1], nodes[0] == nodes[1] ? "  <-- both terminals on the same node (shorted)" : "");
		}
	}

	/** A wire's graph identity is just its position (one electrical point for the whole block).
	 *  A component's graph identity is one of these per lead, so its two terminals can never be
	 *  merged into each other via the component's own body. */
	private record NodeKey(BlockPos pos, Direction side) {
	}

	/** The union-find's resolved node-id assignment, decoupled from any particular {@link Circuit}
	 *  or {@link AcCircuit} instance so both solvers can share exactly the same topology and node
	 *  numbering rather than each recomputing (and potentially disagreeing on) it independently. */
	private record NodeAssignment(Map<Object, Integer> nodeIdByKey, int nodeCount) {
	}

	private static Object keyFor(BlockPos pos, NetworkBlockEntity entity, Direction side) {
		return entity instanceof SingleNodeBlockEntity ? pos : new NodeKey(pos, side);
	}

	private NodeAssignment computeNodeAssignment() {
		Map<Object, Object> parent = new HashMap<>();
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			for (Direction direction : Direction.values()) {
				if (!entity.isConductiveTowards(direction)) continue;
				Object key = keyFor(pos, entity, direction);
				parent.putIfAbsent(key, key);
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
		// Any network a Ground block touches gets anchored to node 0 (always exactly 0V) instead
		// of an arbitrary freshly-allocated node - see rebuild()'s original comment for why.
		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			if (entry.getValue() instanceof GroundBlockEntity) {
				nodeIdByRoot.put(find(parent, entry.getKey()), 0);
			}
		}

		Map<Object, Integer> nodeIdByKey = new HashMap<>();
		int[] nextId = {1}; // node 0 = ground
		for (Object key : parent.keySet()) {
			Object root = find(parent, key);
			int nodeId = nodeIdByRoot.computeIfAbsent(root, r -> nextId[0]++);
			nodeIdByKey.put(key, nodeId);
		}

		return new NodeAssignment(nodeIdByKey, nextId[0]);
	}

	private void rebuild() {
		dirty = false;
		circuit = new Circuit();
		lastComponentNodes.clear();

		NodeAssignment assignment = computeNodeAssignment();
		for (int i = 1; i < assignment.nodeCount(); i++) {
			circuit.addNode();
		}

		for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
			BlockPos pos = entry.getKey();
			NetworkBlockEntity entity = entry.getValue();
			if (entity instanceof ComponentBlockEntity component) {
				int nodeA = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing()));
				int nodeB = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing().getOpposite()));
				component.addToCircuit(circuit, nodeA, nodeB);
				lastComponentNodes.put(pos, new int[] {nodeA, nodeB});
			} else if (entity instanceof OpAmpBlockEntity opAmp) {
				int nodeOut = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.outputFace()));
				int nodeMinus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.minusFace()));
				int nodePlus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.plusFace()));
				opAmp.addToCircuit(circuit, nodeOut, nodeMinus, nodePlus);
			} else if (entity instanceof SingleNodeBlockEntity singleNode) {
				singleNode.bindNode(circuit, assignment.nodeIdByKey().get(pos));
			}
		}
	}

	/** Result of one AC (Bode-plot) sweep: log-spaced frequencies across the AC source's
	 *  configured range, each with the signal/source complex ratio's magnitude (dB) and phase
	 *  (degrees). {@code warning} is non-null, and the three lists empty, if the sweep could not
	 *  be performed at all (e.g. the pinned position wasn't actually an AC Source). */
	public record AcSweepResult(List<Double> freqsHz, List<Double> magnitudesDb, List<Double> phasesDeg, String warning) {
		public static AcSweepResult error(String warning) {
			return new AcSweepResult(List.of(), List.of(), List.of(), warning);
		}
	}

	public AcSweepResult computeAcSweep(BlockPos sourcePos, BlockPos signalPos) {
		if (!(participants.get(sourcePos) instanceof AcSourceBlockEntity acSource)) {
			return AcSweepResult.error("Pinned position is not an AC Source block.");
		}
		if (!acSource.isRedstonePowered()) {
			return AcSweepResult.error("AC Source has no redstone power.");
		}
		NetworkBlockEntity signalEntity = participants.get(signalPos);
		if (signalEntity == null) {
			return AcSweepResult.error("No wired component at the signal position.");
		}

		NodeAssignment assignment = computeNodeAssignment();
		double logMin = Math.log10(acSource.minFrequencyHz());
		double logMax = Math.log10(acSource.maxFrequencyHz());
		Complex sourceAmplitude = Complex.real(acSource.amplitudeVolts());

		List<Double> freqs = new ArrayList<>();
		List<Double> mags = new ArrayList<>();
		List<Double> phases = new ArrayList<>();

		for (int i = 0; i < AC_SWEEP_POINTS; i++) {
			double t = (double) i / (AC_SWEEP_POINTS - 1);
			double freqHz = Math.pow(10, logMin + t * (logMax - logMin));
			double omega = 2 * Math.PI * freqHz;

			AcCircuit acCircuit = new AcCircuit();
			for (int n = 1; n < assignment.nodeCount(); n++) {
				acCircuit.addNode();
			}

			for (Map.Entry<BlockPos, NetworkBlockEntity> entry : participants.entrySet()) {
				BlockPos pos = entry.getKey();
				NetworkBlockEntity entity = entry.getValue();
				if (entity instanceof OpAmpBlockEntity opAmp) {
					int nodeOut = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.outputFace()));
					int nodeMinus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.minusFace()));
					int nodePlus = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.plusFace()));
					opAmp.addToAcCircuit(acCircuit, nodeOut, nodeMinus, nodePlus);
				} else if (entity instanceof ComponentBlockEntity component && entity instanceof AcStampable stampable) {
					int nodeA = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing()));
					int nodeB = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing().getOpposite()));
					stampable.addToAcCircuit(acCircuit, nodeA, nodeB);
				}
				// A plain SingleNodeBlockEntity (wire/ground) contributes no element - it's just a
				// node, exactly as in the transient circuit.
			}

			try {
				acCircuit.solve(omega);
			} catch (ArithmeticException e) {
				return AcSweepResult.error("AC solver error: " + e.getMessage());
			}

			Complex signalVoltage = readAcVoltage(signalPos, signalEntity, assignment, acCircuit);
			Complex h = signalVoltage.divide(sourceAmplitude);

			freqs.add(freqHz);
			mags.add(20 * Math.log10(Math.max(h.magnitude(), 1e-12)));
			phases.add(Math.toDegrees(h.angle()));
		}

		return new AcSweepResult(freqs, mags, phases, null);
	}

	/** Returns whatever quantity an AC-probed signal point should be judged against the source
	 *  amplitude: normally that's the small-signal voltage across the pinned element, but an ideal
	 *  ammeter is electrically a wire, so the voltage across it is identically zero at every
	 *  frequency by construction - probing one for voltage would always show a flat, meaningless
	 *  trace. Its branch current (see {@link AmmeterBlockEntity#acCurrent}) is used instead, which
	 *  makes the resulting ratio a transadmittance rather than a unitless gain, exactly as pinning
	 *  the regular (transient) probe on an ammeter already reads current instead of voltage. */
	private static Complex readAcVoltage(BlockPos pos, NetworkBlockEntity entity, NodeAssignment assignment, AcCircuit acCircuit) {
		if (entity instanceof AmmeterBlockEntity ammeter) {
			return ammeter.acCurrent();
		}
		if (entity instanceof OpAmpBlockEntity opAmp) {
			int nodeOut = assignment.nodeIdByKey().get(new NodeKey(pos, opAmp.outputFace()));
			return acCircuit.getVoltage(nodeOut);
		}
		if (entity instanceof ComponentBlockEntity component) {
			int nodeA = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing()));
			int nodeB = assignment.nodeIdByKey().get(new NodeKey(pos, component.getFacing().getOpposite()));
			return acCircuit.getVoltage(nodeA).subtract(acCircuit.getVoltage(nodeB));
		}
		if (entity instanceof SingleNodeBlockEntity) {
			return acCircuit.getVoltage(assignment.nodeIdByKey().get(pos));
		}
		return Complex.ZERO;
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
