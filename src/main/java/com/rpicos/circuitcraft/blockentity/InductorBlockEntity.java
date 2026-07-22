package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Inductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class InductorBlockEntity extends ComponentBlockEntity {

	// Henries tuned for Minecraft's tick timescale, not real-world inductor ratings - large
	// values are needed for an RL time constant that's actually visible across a few ticks.
	private static final double[] PRESETS_HENRIES = {0.01, 0.1, 1.0, 5.0};

	private int presetIndex = 1;
	private Inductor live;

	public InductorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.INDUCTOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_HENRIES.length;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new Inductor(nodeA, nodeB, PRESETS_HENRIES[presetIndex]);
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		return "Inductor " + PRESETS_HENRIES[presetIndex] + " H";
	}
}
