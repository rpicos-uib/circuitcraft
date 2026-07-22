package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.PowerSupplyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class PowerSupplyBlock extends ComponentBlock {
	public static final MapCodec<PowerSupplyBlock> CODEC = simpleCodec(PowerSupplyBlock::new);

	public PowerSupplyBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<PowerSupplyBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PowerSupplyBlockEntity(pos, state);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PowerSupplyBlockEntity powerSupply) {
			powerSupply.setRedstonePowered(level.hasNeighborSignal(pos));
		}
	}
}
