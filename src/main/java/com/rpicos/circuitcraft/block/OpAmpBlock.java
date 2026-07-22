package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.OpAmpBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Reuses ComponentBlock's FACING placement/rotation/interaction handling as-is: that logic is
 *  entirely block-side and doesn't depend on whether the block entity has two leads or three.
 *  An ideal op-amp has no adjustable preset, so ComponentBlock's empty-hand cycle-preset click
 *  simply no-ops here (its instanceof check against ComponentBlockEntity never matches). */
public class OpAmpBlock extends ComponentBlock {
	public static final MapCodec<OpAmpBlock> CODEC = simpleCodec(OpAmpBlock::new);

	public OpAmpBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<OpAmpBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new OpAmpBlockEntity(pos, state);
	}
}
