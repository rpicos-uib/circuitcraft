package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Memristor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MemristorBlockEntity extends ComponentBlockEntity {

	private static final double RON = 100;
	private static final double ROFF = 10_000;
	// Smaller qMax = faster switching for the same current, so this preset is "switching speed".
	private static final double[] PRESETS_Q_MAX = {1e-4, 1e-3, 1e-2};

	private int presetIndex = 1;
	private Memristor live;
	private double savedFraction = 0.0;

	public MemristorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.MEMRISTOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_Q_MAX.length;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		if (live != null) {
			savedFraction = live.stateFraction();
		}
		live = new Memristor(nodeA, nodeB, RON, ROFF, PRESETS_Q_MAX[presetIndex], savedFraction);
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
