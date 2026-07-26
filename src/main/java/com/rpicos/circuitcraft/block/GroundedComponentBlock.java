package com.rpicos.circuitcraft.block;

import com.rpicos.circuitcraft.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

/** Shared base for a component whose two leads are fixed to the vertical axis (up = the wired
 *  output/input into the rest of the circuit, down = the electrical/physical ground reference)
 *  rather than the usual player-rotatable horizontal FACING every other {@link ComponentBlock}
 *  allows. FACING is still the property {@link ComponentBlockEntity}/{@code CircuitNetworkManager}
 *  key leads off internally - it's just permanently pinned to {@link Direction#UP} here instead
 *  of letting the player choose it, since North/South are reserved for redstone I/O on this
 *  family of block and can't also double as the electrical leads. */
public abstract class GroundedComponentBlock extends ComponentBlock {

	protected GroundedComponentBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, Direction.UP);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		// Orientation is fixed - North/South are reserved for redstone I/O, so this block can't
		// be spun the way a normal ComponentBlock can.
		return state;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).is(ModBlocks.GROUND);
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
		if (!level.isClientSide() && !canSurvive(state, level, pos)) {
			level.destroyBlock(pos, true);
		}
	}
}
