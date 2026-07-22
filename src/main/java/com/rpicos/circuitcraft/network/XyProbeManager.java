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

/** Tracks up to two components/wires/ground blocks each player has pinned with the XY probe: the
 *  older of the two drives the X axis, the newer drives Y - the same pin/promote/evict-oldest
 *  rule {@link ProbeWatchManager} uses for the time-domain probe, just with a 2-channel limit
 *  instead of 3. A separate pin list from ProbeWatchManager's, so holding both probes at once
 *  doesn't have one interfere with the other. */
public final class XyProbeManager {
	public static final int MAX_CHANNELS = 2;

	private static final Map<UUID, List<BlockPos>> PINS = new ConcurrentHashMap<>();

	private XyProbeManager() {
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
			if (pins == null || pins.size() < MAX_CHANNELS) {
				continue;
			}
			BlockPos xPos = pins.get(0);
			BlockPos yPos = pins.get(1);
			if (level.getBlockEntity(xPos) instanceof Probeable x && level.getBlockEntity(yPos) instanceof Probeable y) {
				XyProbeDataPayload payload = new XyProbeDataPayload(
						xPos, x.probeSummary(), (float) x.probeVoltage(), x.historySnapshot(),
						yPos, y.probeSummary(), (float) y.probeVoltage(), y.historySnapshot());
				ServerPlayNetworking.send(player, payload);
			}
		}
	}
}
