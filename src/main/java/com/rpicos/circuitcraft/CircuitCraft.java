package com.rpicos.circuitcraft;

import com.rpicos.circuitcraft.blockentity.NetworkBlockEntity;
import com.rpicos.circuitcraft.blockentity.ValueEditable;
import com.rpicos.circuitcraft.network.CircuitNetworkManager;
import com.rpicos.circuitcraft.network.ComponentValueUpdatePayload;
import com.rpicos.circuitcraft.network.OpenValueEditorPayload;
import com.rpicos.circuitcraft.network.ProbeDataPayload;
import com.rpicos.circuitcraft.network.ProbeWatchManager;
import com.rpicos.circuitcraft.network.XyProbeDataPayload;
import com.rpicos.circuitcraft.network.XyProbeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircuitCraft implements ModInitializer {
	public static final String MOD_ID = "circuitcraft";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("CircuitCraft: initializing analog electronics circuitry");

		ModBlocks.init();
		ModBlockEntities.init();
		ModItems.init();
		ModCreativeTab.init();

		PayloadTypeRegistry.clientboundPlay().register(ProbeDataPayload.TYPE, ProbeDataPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(XyProbeDataPayload.TYPE, XyProbeDataPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(OpenValueEditorPayload.TYPE, OpenValueEditorPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(ComponentValueUpdatePayload.TYPE, ComponentValueUpdatePayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(ComponentValueUpdatePayload.TYPE, (payload, context) -> {
			ServerLevel level = context.player().level();
			if (!(level.getBlockEntity(payload.pos()) instanceof ValueEditable editable)) {
				return;
			}
			editable.applyEditedValues(payload.values());
			if (level.getBlockEntity(payload.pos()) instanceof NetworkBlockEntity networkEntity) {
				// Matches ComponentBlock's own empty-hand cycle-preset path: a value change needs
				// an explicit dirty-mark, since the circuit is only re-stamped from each
				// component's current value when its network is actually rebuilt, not every tick.
				networkEntity.markNetworkDirty();
			}
			StringBuilder message = new StringBuilder();
			for (var field : editable.editableFields()) {
				if (!message.isEmpty()) {
					message.append(", ");
				}
				message.append(field.label()).append("=").append(field.current()).append(field.unit());
			}
			context.player().sendSystemMessage(Component.literal(message.toString()));
		});

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
