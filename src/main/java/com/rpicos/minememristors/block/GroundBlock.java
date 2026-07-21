package com.rpicos.minememristors.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.minememristors.blockentity.GroundBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Ties whatever network it's wired into to a real 0V reference point. Conductive on all six
 *  faces, same as Wire - connect it in with wire like any other network participant. */
public class GroundBlock extends Block implements EntityBlock {
	public static final MapCodec<GroundBlock> CODEC = simpleCodec(GroundBlock::new);

	public GroundBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<GroundBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GroundBlockEntity(pos, state);
	}
}
