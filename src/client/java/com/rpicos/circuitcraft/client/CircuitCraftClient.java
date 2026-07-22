package com.rpicos.circuitcraft.client;

import com.rpicos.circuitcraft.CircuitCraft;
import com.rpicos.circuitcraft.network.OpenValueEditorPayload;
import com.rpicos.circuitcraft.network.ProbeDataPayload;
import com.rpicos.circuitcraft.network.XyProbeDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class CircuitCraftClient implements ClientModInitializer {
	private static final Identifier OSCILLOSCOPE_HUD_ID = CircuitCraft.id("oscilloscope");
	private static final Identifier XY_OSCILLOSCOPE_HUD_ID = CircuitCraft.id("xy_oscilloscope");

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(OSCILLOSCOPE_HUD_ID, new OscilloscopeHud());
		HudElementRegistry.addLast(XY_OSCILLOSCOPE_HUD_ID, new XyOscilloscopeHud());

		ClientPlayNetworking.registerGlobalReceiver(ProbeDataPayload.TYPE,
				(payload, context) -> ProbeClientState.update(payload));
		ClientPlayNetworking.registerGlobalReceiver(XyProbeDataPayload.TYPE,
				(payload, context) -> XyProbeClientState.update(payload));
		ClientPlayNetworking.registerGlobalReceiver(OpenValueEditorPayload.TYPE,
				(payload, context) -> Minecraft.getInstance().setScreenAndShow(
						new ComponentValueScreen(payload.pos(), payload.fields())));
	}
}
