package com.rpicos.circuitcraft.network;

import com.rpicos.circuitcraft.blockentity.ValueEditable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Shared by every block whose right-click interaction cycles a preset: a shift-right-click with
 *  an empty hand instead opens the value-editor screen (the same interaction spirit as editing a
 *  sign) when the clicked block entity supports it, in place of the usual cycle-to-next-preset
 *  behavior. */
public final class ValueEditorOpener {
	private ValueEditorOpener() {
	}

	/** Returns true if the editor was opened (caller should skip its own cycle-preset behavior). */
	public static boolean tryOpen(BlockEntity blockEntity, BlockPos pos, Player player) {
		if (!player.isShiftKeyDown()) {
			return false;
		}
		if (!(blockEntity instanceof ValueEditable editable)) {
			return false;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return false;
		}
		ServerPlayNetworking.send(serverPlayer, new OpenValueEditorPayload(pos, editable.editableFields()));
		return true;
	}
}
