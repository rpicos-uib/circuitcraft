package com.rpicos.circuitcraft.network;

import com.rpicos.circuitcraft.blockentity.Probeable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks up to {@value #MAX_CHANNELS} components each player has pinned with the probe
 *  (right-click a component to pin it/bump it to newest, shift+right-click to unpin), and streams
 *  every pinned component's live readout to that player each tick - independent of where they're
 *  currently looking. Pinning the same position again just moves it to newest without duplicating;
 *  pinning past the limit drops the oldest channel. */
public final class ProbeWatchManager {
	public static final int MAX_CHANNELS = 3;

	private static final Map<UUID, List<BlockPos>> PINS = new ConcurrentHashMap<>();

	private ProbeWatchManager() {
	}

	public static void pin(ServerPlayer player, BlockPos pos) {
		List<BlockPos> pins = PINS.computeIfAbsent(player.getUUID(), id -> new ArrayList<>());
		BlockPos key = pos.immutable();
		pins.remove(key);
		pins.add(key);
		while (pins.size() > MAX_CHANNELS) {
			pins.remove(0);
		}
	}

	public static void unpin(ServerPlayer player, BlockPos pos) {
		List<BlockPos> pins = PINS.get(player.getUUID());
		if (pins != null) {
			pins.remove(pos);
		}
	}

	public static void clear(UUID playerId) {
		PINS.remove(playerId);
	}

	public static void tick(ServerLevel level) {
		for (ServerPlayer player : level.players()) {
			List<BlockPos> pins = PINS.get(player.getUUID());
			if (pins == null || pins.isEmpty()) {
				continue;
			}
			// Only meaningful once the list is full: with room to spare, pinning one more just
			// adds a channel rather than evicting anything.
			boolean atCapacity = pins.size() >= MAX_CHANNELS;
			BlockPos oldest = atCapacity ? pins.get(0) : null;
			for (BlockPos pos : List.copyOf(pins)) {
				if (level.getBlockEntity(pos) instanceof Probeable component) {
					List<Float> history = component.historySnapshot();
					ProbeDataPayload payload = new ProbeDataPayload(
							pos, component.probeSummary(), (float) component.probeVoltage(),
							(float) component.probeCurrent(), history, pos.equals(oldest));
					ServerPlayNetworking.send(player, payload);
				}
			}
		}
	}
}
