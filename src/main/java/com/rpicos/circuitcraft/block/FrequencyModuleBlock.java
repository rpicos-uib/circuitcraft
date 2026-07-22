package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.FrequencyModuleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FrequencyModuleBlock extends ModuleBlock {
	public static final MapCodec<FrequencyModuleBlock> CODEC = simpleCodec(FrequencyModuleBlock::new);

	public FrequencyModuleBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<FrequencyModuleBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FrequencyModuleBlockEntity(pos, state);
	}
}
