package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.AcSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class AcSourceBlock extends ComponentBlock {
	public static final MapCodec<AcSourceBlock> CODEC = simpleCodec(AcSourceBlock::new);

	public AcSourceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<AcSourceBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AcSourceBlockEntity(pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AcSourceBlockEntity acSource) {
			acSource.setRedstonePowered(level.hasNeighborSignal(pos));
		}
	}
}
