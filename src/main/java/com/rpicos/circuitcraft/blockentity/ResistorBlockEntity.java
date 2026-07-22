package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Resistor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ResistorBlockEntity extends ComponentBlockEntity implements ValueEditable {

	private static final double[] PRESETS_OHMS = {10, 100, 1_000, 10_000};

	private int presetIndex = 1;
	private double resistanceOhms = PRESETS_OHMS[presetIndex];
	private Resistor live;

	public ResistorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RESISTOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_OHMS.length;
		resistanceOhms = PRESETS_OHMS[presetIndex];
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Resistance", "ohm",
				PRESETS_OHMS[0], PRESETS_OHMS[PRESETS_OHMS.length - 1], resistanceOhms));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		resistanceOhms = Math.clamp(values.get(0), PRESETS_OHMS[0], PRESETS_OHMS[PRESETS_OHMS.length - 1]);
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new Resistor(nodeA, nodeB, resistanceOhms);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : probeVoltage() / resistanceOhms;
	}

	@Override
	public String probeSummary() {
		return "Resistor " + resistanceOhms + " ohm";
	}
}
