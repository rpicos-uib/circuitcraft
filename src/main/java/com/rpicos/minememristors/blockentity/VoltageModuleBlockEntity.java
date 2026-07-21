package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class VoltageModuleBlockEntity extends ModuleBlockEntity {

	private static final double[] PRESETS_VOLTS = {1.5, 5, 9, 12, 24};

	public VoltageModuleBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.VOLTAGE_MODULE, pos, state);
	}

	@Override
	protected Channel channel() {
		return Channel.VOLTAGE;
	}

	@Override
	protected double[] presets() {
		return PRESETS_VOLTS;
	}

	@Override
	protected String unitSuffix() {
		return "V";
	}
}
