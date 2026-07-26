package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.NmosBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class NmosBlock extends ComponentBlock {
	public static final MapCodec<NmosBlock> CODEC = simpleCodec(NmosBlock::new);

	public NmosBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<NmosBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new NmosBlockEntity(pos, state);
	}
}
