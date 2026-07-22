package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.VoltageModuleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class VoltageModuleBlock extends ModuleBlock {
	public static final MapCodec<VoltageModuleBlock> CODEC = simpleCodec(VoltageModuleBlock::new);

	public VoltageModuleBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<VoltageModuleBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VoltageModuleBlockEntity(pos, state);
	}
}
