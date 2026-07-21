package com.rpicos.minememristors.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A configuration block (Voltage/Frequency module) that cycles a preset value on right-click and
 * pushes it into any adjacent {@link FunctionGeneratorBlockEntity}. Same-kind modules touching each
 * other relay a single shared value along the whole connected chain, so one control can drive
 * several generators - whichever module in the chain was right-clicked most recently wins and
 * propagates outward one hop per tick (imperceptibly fast at 20 ticks/second for any normal build).
 */
public abstract class ModuleBlockEntity extends BlockEntity {

	private int presetIndex;
	private long ownSetTick;
	private double resolvedValue;
	private long resolvedSourceTick;

	protected ModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.resolvedValue = presets()[presetIndex];
	}

	protected abstract double[] presets();

	protected abstract String unitSuffix();

	protected abstract void applyToGenerator(FunctionGeneratorBlockEntity generator, double value);

	public void cyclePreset(long currentTick) {
		presetIndex = (presetIndex + 1) % presets().length;
		ownSetTick = currentTick;
		setChanged();
	}

	public String summary() {
		return String.format("%.2f%s", resolvedValue, unitSuffix());
	}

	/** Called once per tick (see the owning Block's {@code getTicker}). */
	public void tickModule(Level level, BlockPos pos, long currentTick) {
		double bestValue = presets()[presetIndex];
		long bestTick = ownSetTick;
		for (Direction direction : Direction.values()) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof ModuleBlockEntity other
					&& other.getClass() == getClass() && other.resolvedSourceTick > bestTick) {
				bestTick = other.resolvedSourceTick;
				bestValue = other.resolvedValue;
			}
		}
		resolvedValue = bestValue;
		resolvedSourceTick = bestTick;

		for (Direction direction : Direction.values()) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof FunctionGeneratorBlockEntity generator) {
				applyToGenerator(generator, resolvedValue);
			}
		}
	}
}
