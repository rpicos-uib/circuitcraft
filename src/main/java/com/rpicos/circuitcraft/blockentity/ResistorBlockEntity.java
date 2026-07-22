package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Resistor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ResistorBlockEntity extends ComponentBlockEntity {

	private static final double[] PRESETS_OHMS = {10, 100, 1_000, 10_000};

	private int presetIndex = 1;
	private Resistor live;

	public ResistorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.RESISTOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_OHMS.length;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new Resistor(nodeA, nodeB, PRESETS_OHMS[presetIndex]);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : probeVoltage() / PRESETS_OHMS[presetIndex];
	}

	@Override
	public String probeSummary() {
		return "Resistor " + PRESETS_OHMS[presetIndex] + " ohm";
	}
}
