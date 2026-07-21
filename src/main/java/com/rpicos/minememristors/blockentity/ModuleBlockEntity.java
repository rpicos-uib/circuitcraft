package com.rpicos.minememristors.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A configuration block (Voltage/Frequency module) that cycles a preset value on right-click and
 * pushes it into any adjacent {@link FunctionGeneratorBlockEntity}.
 *
 * <p>Two independent values - voltage and frequency - are relayed through a connected chain of
 * modules, regardless of concrete kind: a Voltage module "authors" the voltage channel (its own
 * dial is the value, timestamped by when it was last clicked) but only relays whatever it hears
 * for the frequency channel, and vice versa for a Frequency module. This lets a Frequency module
 * sit in the middle of a chain without blocking a Voltage module's value from reaching a generator
 * further along - both channels pass through every module, only interpreted/authored by the one
 * that owns it. Whichever module actually authored a channel most recently wins across the whole
 * reachable chain, propagating outward one hop per tick (imperceptibly fast at 20 ticks/second for
 * any normal build).
 */
public abstract class ModuleBlockEntity extends BlockEntity {

	public enum Channel {VOLTAGE, FREQUENCY}

	private record ChannelValue(double value, long sourceTick) {
	}

	private int presetIndex;
	private long ownSetTick;

	// "Nobody has authored this yet" placeholders: Long.MIN_VALUE always loses to any module that
	// actually owns the channel (whose tick starts at 0 from construction), but still gives every
	// module a sensible value to relay before any real source is reachable.
	private ChannelValue voltage = new ChannelValue(FunctionGeneratorBlockEntity.DEFAULT_AMPLITUDE_VOLTS, Long.MIN_VALUE);
	private ChannelValue frequency = new ChannelValue(FunctionGeneratorBlockEntity.DEFAULT_FREQUENCY_HZ, Long.MIN_VALUE);

	protected ModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected abstract Channel channel();

	protected abstract double[] presets();

	protected abstract String unitSuffix();

	public void cyclePreset(long currentTick) {
		presetIndex = (presetIndex + 1) % presets().length;
		ownSetTick = currentTick;
		setChanged();
	}

	public String summary() {
		ChannelValue mine = channel() == Channel.VOLTAGE ? voltage : frequency;
		return String.format("%.2f%s", mine.value(), unitSuffix());
	}

	/** Called once per tick (see the owning Block's {@code getTicker}). */
	public void tickModule(Level level, BlockPos pos, long currentTick) {
		ChannelValue bestVoltage = channel() == Channel.VOLTAGE
				? new ChannelValue(presets()[presetIndex], ownSetTick)
				: voltage;
		ChannelValue bestFrequency = channel() == Channel.FREQUENCY
				? new ChannelValue(presets()[presetIndex], ownSetTick)
				: frequency;

		for (Direction direction : Direction.values()) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof ModuleBlockEntity other) {
				if (other.voltage.sourceTick() > bestVoltage.sourceTick()) {
					bestVoltage = other.voltage;
				}
				if (other.frequency.sourceTick() > bestFrequency.sourceTick()) {
					bestFrequency = other.frequency;
				}
			}
		}
		voltage = bestVoltage;
		frequency = bestFrequency;

		for (Direction direction : Direction.values()) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof FunctionGeneratorBlockEntity generator) {
				generator.setAmplitude(voltage.value());
				generator.setFrequency(frequency.value());
			}
		}
	}
}
