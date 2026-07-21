package com.rpicos.minememristors.item;

import com.rpicos.minememristors.blockentity.ComponentBlockEntity;
import com.rpicos.minememristors.network.ProbeWatchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Oscilloscope probe: right-click a component to pin it as one of up to
 * {@value ProbeWatchManager#MAX_CHANNELS} channels shown simultaneously on the HUD (pinning a
 * 4th evicts the oldest); shift+right-click unpins it. The HUD is rendered while the probe is
 * held, like looking at a map.
 */
public class ProbeItem extends Item {
	public ProbeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		if (level.getBlockEntity(pos) instanceof ComponentBlockEntity component
				&& context.getPlayer() instanceof ServerPlayer player) {
			String readout = String.format("%s | V=%.2fV  I=%.4fA",
					component.probeSummary(), component.probeVoltage(), component.probeCurrent());
			if (player.isShiftKeyDown()) {
				ProbeWatchManager.unpin(player, pos);
				player.sendOverlayMessage(Component.literal("Unpinned: " + readout));
			} else {
				ProbeWatchManager.pin(player, pos);
				player.sendOverlayMessage(Component.literal("Pinned: " + readout));
			}
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}
}
