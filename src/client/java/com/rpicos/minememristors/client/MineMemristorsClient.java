package com.rpicos.minememristors.client;

import com.rpicos.minememristors.MineMemristors;
import com.rpicos.minememristors.network.ProbeDataPayload;
import com.rpicos.minememristors.network.XyProbeDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public class MineMemristorsClient implements ClientModInitializer {
	private static final Identifier OSCILLOSCOPE_HUD_ID = MineMemristors.id("oscilloscope");
	private static final Identifier XY_OSCILLOSCOPE_HUD_ID = MineMemristors.id("xy_oscilloscope");

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(OSCILLOSCOPE_HUD_ID, new OscilloscopeHud());
		HudElementRegistry.addLast(XY_OSCILLOSCOPE_HUD_ID, new XyOscilloscopeHud());

		ClientPlayNetworking.registerGlobalReceiver(ProbeDataPayload.TYPE,
				(payload, context) -> ProbeClientState.update(payload));
		ClientPlayNetworking.registerGlobalReceiver(XyProbeDataPayload.TYPE,
				(payload, context) -> XyProbeClientState.update(payload));
	}
}
