package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.VccsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class VccsBlock extends GroundedComponentBlock {
	public static final MapCodec<VccsBlock> CODEC = simpleCodec(VccsBlock::new);

	public VccsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<VccsBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VccsBlockEntity(pos, state);
	}
}
