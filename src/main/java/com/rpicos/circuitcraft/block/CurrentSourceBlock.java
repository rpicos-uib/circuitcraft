package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.CurrentSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

/** An ideal independent current source. Redstone-gated exactly like {@link PowerSupplyBlock} -
 *  an inactive source is an open circuit rather than a live 0A source, for the same reason. */
public class CurrentSourceBlock extends ComponentBlock {
	public static final MapCodec<CurrentSourceBlock> CODEC = simpleCodec(CurrentSourceBlock::new);

	public CurrentSourceBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<CurrentSourceBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CurrentSourceBlockEntity(pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CurrentSourceBlockEntity currentSource) {
			currentSource.setRedstonePowered(level.hasNeighborSignal(pos));
		}
	}
}
