package com.rpicos.circuitcraft.client;

import com.rpicos.circuitcraft.network.XyProbeDataPayload;

/** Latest X-Y oscilloscope pair received from the server, for the HUD to render. Only one pair
 *  is ever active per player, unlike the time-domain probe's up-to-3-channel map. */
final class XyProbeClientState {
	private static final long STALE_AFTER_MILLIS = 1000;

	private static volatile XyProbeDataPayload latest;
	private static volatile long lastReceivedAtMillis;

	private XyProbeClientState() {
	}

	static void update(XyProbeDataPayload payload) {
		latest = payload;
		lastReceivedAtMillis = System.currentTimeMillis();
	}

	/** Null if nothing has arrived recently (fewer than 2 channels pinned, or data went stale). */
	static XyProbeDataPayload current() {
		XyProbeDataPayload payload = latest;
		if (payload == null) {
			return null;
		}
		return System.currentTimeMillis() - lastReceivedAtMillis > STALE_AFTER_MILLIS ? null : payload;
	}
}
