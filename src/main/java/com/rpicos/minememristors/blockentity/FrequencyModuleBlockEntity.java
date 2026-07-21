package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FrequencyModuleBlockEntity extends ModuleBlockEntity {

	private static final double[] PRESETS_HZ = {0.5, 1, 2, 5, 10};

	public FrequencyModuleBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FREQUENCY_MODULE, pos, state);
	}

	@Override
	protected double[] presets() {
		return PRESETS_HZ;
	}

	@Override
	protected String unitSuffix() {
		return "Hz";
	}

	@Override
	protected void applyToGenerator(FunctionGeneratorBlockEntity generator, double value) {
		generator.setFrequency(value);
	}
}
