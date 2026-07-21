package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import com.rpicos.minememristors.sim.Circuit;
import com.rpicos.minememristors.sim.VoltageSource;
import com.rpicos.minememristors.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PowerSupplyBlockEntity extends ComponentBlockEntity {

	private static final double[] PRESETS_VOLTS = {1.5, 5, 9, 12, 24};

	private int presetIndex = 1;
	private VoltageSource live;
	private boolean redstonePowered = false;

	public PowerSupplyBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.POWER_SUPPLY, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_VOLTS.length;
	}

	/** Called by {@link com.rpicos.minememristors.block.PowerSupplyBlock#neighborChanged} whenever
	 *  a redstone neighbor changes. Only rebuilds the circuit if the powered state actually flips,
	 *  so idle redstone dust nearby doesn't force a rebuild every tick. */
	public void setRedstonePowered(boolean powered) {
		if (redstonePowered != powered) {
			redstonePowered = powered;
			markNetworkDirty();
		}
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		bindNodes(circuit, nodeA, nodeB);
		if (!redstonePowered) {
			// Left un-stamped: an inactive supply behaves as an open circuit rather than a 0V
			// source, so a still-being-wired power supply can't short itself into a singular
			// matrix before the builder is ready to switch it on.
			live = null;
			return;
		}
		live = new VoltageSource(nodeA, nodeB, Waveform.dc(PRESETS_VOLTS[presetIndex]));
		circuit.add(live);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = "Power Supply " + PRESETS_VOLTS[presetIndex] + " V DC";
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
