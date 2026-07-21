package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import com.rpicos.minememristors.sim.Circuit;
import com.rpicos.minememristors.sim.VoltageSource;
import com.rpicos.minememristors.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FunctionGeneratorBlockEntity extends ComponentBlockEntity {

	private record Preset(String name, Waveform waveform) {
	}

	private static final Preset[] PRESETS = {
			new Preset("sine 5V 1Hz", Waveform.sine(5, 1, 0, 0)),
			new Preset("square 5V 1Hz", Waveform.square(5, 1, 0)),
			new Preset("triangle 5V 1Hz", Waveform.triangle(5, 1, 0)),
			new Preset("sine 5V 5Hz", Waveform.sine(5, 5, 0, 0)),
	};

	private int presetIndex = 0;
	private VoltageSource live;
	private boolean redstonePowered = false;

	public FunctionGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FUNCTION_GENERATOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS.length;
	}

	/** Called by {@link com.rpicos.minememristors.block.FunctionGeneratorBlock#neighborChanged}
	 *  whenever a redstone neighbor changes; see {@link PowerSupplyBlockEntity#setRedstonePowered}
	 *  for why an inactive source is left un-stamped rather than driven at 0V. */
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
			live = null;
			return;
		}
		live = new VoltageSource(nodeA, nodeB, PRESETS[presetIndex].waveform());
		circuit.add(live);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = "Function Generator: " + PRESETS[presetIndex].name();
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
