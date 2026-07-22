package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.WireBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** A simple conductive block: connects to any neighbouring wire or component terminal on all six faces. */
public class WireBlock extends Block implements EntityBlock {
	public static final MapCodec<WireBlock> CODEC = simpleCodec(WireBlock::new);

	public WireBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<WireBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WireBlockEntity(pos, state);
	}
}
