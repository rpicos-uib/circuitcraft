package com.rpicos.circuitcraft.item;

import com.rpicos.circuitcraft.blockentity.Probeable;
import com.rpicos.circuitcraft.network.XyProbeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * X-Y oscilloscope probe: a second, dedicated probe that plots one pinned channel against
 * another (a Lissajous-style X-Y trace) instead of either against time. Right-click a
 * component, wire, or ground to pin it (the older of the two current pins becomes X, the
 * newer Y; pinning a third evicts the oldest); shift+right-click unpins it. Independent of the
 * time-domain Probe's own pins - holding both at once shows both HUDs simultaneously.
 */
public class XyProbeItem extends Item {
	public XyProbeItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		BlockPos pos = context.getClickedPos();
		if (level.getBlockEntity(pos) instanceof Probeable component
				&& context.getPlayer() instanceof ServerPlayer player) {
			String readout = String.format("%s | V=%.2fV", component.probeSummary(), component.probeVoltage());
			if (player.isShiftKeyDown()) {
				XyProbeManager.unpin(player, pos);
				player.sendOverlayMessage(Component.literal("Unpinned (XY): " + readout));
			} else {
				XyProbeManager.pin(player, pos);
				player.sendOverlayMessage(Component.literal("Pinned (XY): " + readout));
			}
			return InteractionResult.SUCCESS_SERVER;
		}

		return InteractionResult.PASS;
	}
}
