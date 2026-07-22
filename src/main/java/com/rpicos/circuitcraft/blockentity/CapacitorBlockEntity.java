package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Capacitor;
import com.rpicos.circuitcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CapacitorBlockEntity extends ComponentBlockEntity implements ValueEditable {

	private static final double[] PRESETS_FARADS = {1e-6, 10e-6, 100e-6, 1_000e-6};
	private static final double MIN_MICROFARADS = PRESETS_FARADS[0] * 1e6;
	private static final double MAX_MICROFARADS = PRESETS_FARADS[PRESETS_FARADS.length - 1] * 1e6;

	private int presetIndex = 1;
	private double capacitanceFarads = PRESETS_FARADS[presetIndex];
	private Capacitor live;

	public CapacitorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPACITOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_FARADS.length;
		capacitanceFarads = PRESETS_FARADS[presetIndex];
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Capacitance", "uF",
				MIN_MICROFARADS, MAX_MICROFARADS, capacitanceFarads * 1e6));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		double microfarads = Math.clamp(values.get(0), MIN_MICROFARADS, MAX_MICROFARADS);
		capacitanceFarads = microfarads / 1e6;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new Capacitor(nodeA, nodeB, capacitanceFarads);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		return "Capacitor " + (capacitanceFarads * 1e6) + " uF";
	}
}
