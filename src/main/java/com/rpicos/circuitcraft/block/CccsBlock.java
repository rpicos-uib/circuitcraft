package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.CccsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** {@link GroundedComponentBlock} doesn't restrict how many of the six faces its block entity
 *  actually treats as conductive (that's decided entirely by the block entity's own {@code
 *  isConductiveTowards}) - it only enforces the pinned-FACING/must-sit-on-Ground placement
 *  rule, which a CCCS needs exactly as much as a two- or three-face grounded component does. */
public class CccsBlock extends GroundedComponentBlock {
	public static final MapCodec<CccsBlock> CODEC = simpleCodec(CccsBlock::new);

	public CccsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<CccsBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CccsBlockEntity(pos, state);
	}
}
