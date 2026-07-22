package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.AmmeterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class AmmeterBlock extends ComponentBlock {
	public static final MapCodec<AmmeterBlock> CODEC = simpleCodec(AmmeterBlock::new);

	public AmmeterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<AmmeterBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AmmeterBlockEntity(pos, state);
	}
}
