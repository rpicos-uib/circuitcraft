package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Memristor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MemristorBlockEntity extends ComponentBlockEntity implements ValueEditable {

	private static final double MIN_RON = 10;
	private static final double MAX_RON = 1_000;
	private static final double MIN_ROFF = 1_000;
	private static final double MAX_ROFF = 100_000;
	// Smaller qMax = faster switching for the same current, so this preset is "switching speed".
	private static final double[] PRESETS_Q_MAX = {1e-4, 1e-3, 1e-2};
	private static final double MIN_Q_MAX = PRESETS_Q_MAX[0];
	private static final double MAX_Q_MAX = PRESETS_Q_MAX[PRESETS_Q_MAX.length - 1];

	private int presetIndex = 1;
	private double ronOhms = 100;
	private double roffOhms = 10_000;
	private double qMax = PRESETS_Q_MAX[presetIndex];
	private Memristor live;
	private double savedFraction = 0.0;

	public MemristorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MEMRISTOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		// Empty-hand right-click still only cycles the "switching speed" (qMax) preset, matching
		// the mod's existing worked-experiment writeups; Ron/Roff are only reachable through the
		// shift-right-click value editor (see editableFields() below), since neither one had a
		// preset cycle of its own before this.
		presetIndex = (presetIndex + 1) % PRESETS_Q_MAX.length;
		qMax = PRESETS_Q_MAX[presetIndex];
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(
				new EditableField("R_on", "ohm", MIN_RON, MAX_RON, ronOhms),
				new EditableField("R_off", "ohm", MIN_ROFF, MAX_ROFF, roffOhms),
				new EditableField("Switching charge (q_max)", "C", MIN_Q_MAX, MAX_Q_MAX, qMax)
		);
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		ronOhms = Math.clamp(values.get(0), MIN_RON, MAX_RON);
		roffOhms = Math.clamp(values.get(1), MIN_ROFF, MAX_ROFF);
		qMax = Math.clamp(values.get(2), MIN_Q_MAX, MAX_Q_MAX);
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		if (live != null) {
			savedFraction = live.stateFraction();
		}
		live = new Memristor(nodeA, nodeB, ronOhms, roffOhms, qMax, savedFraction);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : probeVoltage() / live.resistance();
	}

	@Override
	public String probeSummary() {
		if (live == null) return "Memristor";
		return "Memristor R=" + Math.round(live.resistance()) + " ohm (state " + Math.round(live.stateFraction() * 100) + "%)";
	}
}
