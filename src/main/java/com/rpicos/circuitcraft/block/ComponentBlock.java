package com.rpicos.circuitcraft.block;

import com.rpicos.circuitcraft.blockentity.ComponentBlockEntity;
import com.rpicos.circuitcraft.network.ValueEditorOpener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Base for every two-terminal electrical component: a single block oriented along FACING,
 *  its leads being the block's front and back faces. */
public abstract class ComponentBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	protected ComponentBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
	}

	@Override
	public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		// Default block behavior falls back to useWithoutItem() (cycling the preset) for ANY item
		// in hand, not just a truly empty one - which silently consumes the interaction and stops
		// the held item's own useOn() from ever running (e.g. the Probe's pin logic). Only defer to
		// the empty-hand cycle-preset behavior when the hand is actually empty.
		return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (level.getBlockEntity(pos) instanceof ComponentBlockEntity component) {
			if (ValueEditorOpener.tryOpen(component, pos, player)) {
				return InteractionResult.SUCCESS_SERVER;
			}
			component.cyclePreset();
			component.markNetworkDirty();
			player.sendOverlayMessage(Component.literal(component.probeSummary()));
		}
		return InteractionResult.SUCCESS_SERVER;
	}
}
