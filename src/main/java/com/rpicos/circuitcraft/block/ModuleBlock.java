package com.rpicos.circuitcraft.block;

import com.rpicos.circuitcraft.blockentity.ModuleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Base for the Voltage/Frequency configuration modules: undirected cubes (no FACING - any of
 *  their six faces can touch a Function Generator or a same-kind module to relay a value). */
public abstract class ModuleBlock extends Block implements EntityBlock {

	protected ModuleBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		// See ComponentBlock's identical override: without this, the default block behavior falls
		// back to useWithoutItem() for ANY held item, silently consuming clicks meant for the item
		// itself (e.g. the Probe) instead of letting them reach it.
		return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (level.getBlockEntity(pos) instanceof ModuleBlockEntity module) {
			module.cyclePreset(level.getGameTime());
			player.sendOverlayMessage(Component.literal(module.summary()));
		}
		return InteractionResult.SUCCESS_SERVER;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (lvl, pos, st, be) -> {
			if (be instanceof ModuleBlockEntity module) {
				module.tickModule(lvl, pos, lvl.getGameTime());
			}
		};
	}
}
