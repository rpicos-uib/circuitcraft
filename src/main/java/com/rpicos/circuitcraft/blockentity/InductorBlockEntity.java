package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Inductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class InductorBlockEntity extends ComponentBlockEntity implements ValueEditable {

	// Henries tuned for Minecraft's tick timescale, not real-world inductor ratings - large
	// values are needed for an RL time constant that's actually visible across a few ticks.
	private static final double[] PRESETS_HENRIES = {0.01, 0.1, 1.0, 5.0};

	private int presetIndex = 1;
	private double inductanceHenries = PRESETS_HENRIES[presetIndex];
	private Inductor live;

	public InductorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.INDUCTOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_HENRIES.length;
		inductanceHenries = PRESETS_HENRIES[presetIndex];
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Inductance", "H",
				PRESETS_HENRIES[0], PRESETS_HENRIES[PRESETS_HENRIES.length - 1], inductanceHenries));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		inductanceHenries = Math.clamp(values.get(0), PRESETS_HENRIES[0], PRESETS_HENRIES[PRESETS_HENRIES.length - 1]);
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new Inductor(nodeA, nodeB, inductanceHenries);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		return "Inductor " + inductanceHenries + " H";
	}
}
