package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.DiodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class DiodeBlock extends ComponentBlock {
	public static final MapCodec<DiodeBlock> CODEC = simpleCodec(DiodeBlock::new);

	public DiodeBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<DiodeBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DiodeBlockEntity(pos, state);
	}
}
