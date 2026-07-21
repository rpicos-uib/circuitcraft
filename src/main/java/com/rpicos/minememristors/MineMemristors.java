package com.rpicos.minememristors;

import com.rpicos.minememristors.network.CircuitNetworkManager;
import com.rpicos.minememristors.network.ProbeDataPayload;
import com.rpicos.minememristors.network.ProbeWatchManager;
import com.rpicos.minememristors.network.XyProbeDataPayload;
import com.rpicos.minememristors.network.XyProbeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineMemristors implements ModInitializer {
	public static final String MOD_ID = "minememristors";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Mine Memristors: initializing analog electronics circuitry");

		ModBlocks.init();
		ModBlockEntities.init();
		ModItems.init();
		ModCreativeTab.init();

		PayloadTypeRegistry.clientboundPlay().register(ProbeDataPayload.TYPE, ProbeDataPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(XyProbeDataPayload.TYPE, XyProbeDataPayload.STREAM_CODEC);

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ProbeWatchManager.clear(handler.getPlayer().getUUID());
			XyProbeManager.clear(handler.getPlayer().getUUID());
		});

		ServerTickEvents.END_LEVEL_TICK.register(level -> {
			CircuitNetworkManager.forLevel(level).tick(level);
			ProbeWatchManager.tick(level);
			XyProbeManager.tick(level);
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
