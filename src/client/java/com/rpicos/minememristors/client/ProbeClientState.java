package com.rpicos.minememristors.client;

import com.rpicos.minememristors.network.ProbeDataPayload;
import net.minecraft.core.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Latest oscilloscope data received from the server, keyed by channel position, for the HUD to
 *  render up to {@link com.rpicos.minememristors.network.ProbeWatchManager#MAX_CHANNELS} channels
 *  at once. A channel simply stops being returned once its updates go stale (the server only keeps
 *  sending data for channels the player still has pinned), so there's no separate "unpin" message
 *  to track client-side. */
final class ProbeClientState {
	private static final long STALE_AFTER_MILLIS = 1000;

	private record Entry(ProbeDataPayload payload, long receivedAtMillis) {
	}

	private static final Map<BlockPos, Entry> CHANNELS = new ConcurrentHashMap<>();

	private ProbeClientState() {
	}

	static void update(ProbeDataPayload payload) {
		CHANNELS.put(payload.pos(), new Entry(payload, System.currentTimeMillis()));
	}

	/** Up to MAX_CHANNELS live channels, in a stable (position-ordered) sequence, oldest stale
	 *  entries pruned first. */
	static List<ProbeDataPayload> currentChannels() {
		long now = System.currentTimeMillis();
		CHANNELS.values().removeIf(entry -> now - entry.receivedAtMillis() > STALE_AFTER_MILLIS);
		return CHANNELS.values().stream()
				.map(Entry::payload)
				.sorted(Comparator.<ProbeDataPayload>comparingInt(data -> data.pos().getX())
						.thenComparingInt(data -> data.pos().getY())
						.thenComparingInt(data -> data.pos().getZ()))
				.toList();
	}
}
