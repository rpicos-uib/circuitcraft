package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Capacitor;
import com.rpicos.circuitcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CapacitorBlockEntity extends ComponentBlockEntity {

	private static final double[] PRESETS_FARADS = {1e-6, 10e-6, 100e-6, 1_000e-6};

	private int presetIndex = 1;
	private Capacitor live;

	public CapacitorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CAPACITOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_FARADS.length;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new Capacitor(nodeA, nodeB, PRESETS_FARADS[presetIndex]);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		return "Capacitor " + (PRESETS_FARADS[presetIndex] * 1e6) + " uF";
	}
}
