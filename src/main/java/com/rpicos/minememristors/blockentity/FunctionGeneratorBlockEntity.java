package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import com.rpicos.minememristors.sim.Circuit;
import com.rpicos.minememristors.sim.VoltageSource;
import com.rpicos.minememristors.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FunctionGeneratorBlockEntity extends ComponentBlockEntity {

	private enum Kind {SINE, SQUARE, TRIANGLE}

	private static final Kind[] KINDS = Kind.values();
	// Package-visible: ModuleBlockEntity uses these as the "nobody has set this yet" baseline for
	// whichever channel a given module doesn't own.
	static final double DEFAULT_AMPLITUDE_VOLTS = 5;
	static final double DEFAULT_FREQUENCY_HZ = 1;

	private int kindIndex = 0;
	// Overridden by an adjacent Voltage/Frequency module (see ModuleBlockEntity); these are plain
	// defaults so the generator still produces a signal with no modules attached at all.
	private double amplitudeVolts = DEFAULT_AMPLITUDE_VOLTS;
	private double frequencyHz = DEFAULT_FREQUENCY_HZ;
	private VoltageSource live;
	private boolean redstonePowered = false;

	public FunctionGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FUNCTION_GENERATOR, pos, state);
	}

	@Override
	public void cyclePreset() {
		kindIndex = (kindIndex + 1) % KINDS.length;
		markNetworkDirty();
	}

	/** Called by an adjacent {@link VoltageModuleBlockEntity} to set the output amplitude. */
	public void setAmplitude(double volts) {
		if (amplitudeVolts != volts) {
			amplitudeVolts = volts;
			markNetworkDirty();
		}
	}

	/** Called by an adjacent {@link FrequencyModuleBlockEntity} to set the output frequency. */
	public void setFrequency(double hz) {
		if (frequencyHz != hz) {
			frequencyHz = hz;
			markNetworkDirty();
		}
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
		Waveform waveform = switch (KINDS[kindIndex]) {
			case SINE -> Waveform.sine(amplitudeVolts, frequencyHz, 0, 0);
			case SQUARE -> Waveform.square(amplitudeVolts, frequencyHz, 0);
			case TRIANGLE -> Waveform.triangle(amplitudeVolts, frequencyHz, 0);
		};
		live = new VoltageSource(nodeA, nodeB, waveform);
		circuit.add(live);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = String.format("Function Generator: %s %.1fV %.2fHz",
				KINDS[kindIndex].toString().toLowerCase(), amplitudeVolts, frequencyHz);
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
