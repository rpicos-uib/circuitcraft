package com.rpicos.circuitcraft.network;

import com.rpicos.circuitcraft.CircuitCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Client -> server: the values a player typed into the value-editor screen for the component at
 *  {@code pos}, in the same order that component's own {@code editableFields()} reported them.
 *  The server must clamp these itself rather than trust the client's own display-side clamping. */
public record ComponentValueUpdatePayload(BlockPos pos, List<Double> values) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ComponentValueUpdatePayload> TYPE =
			new CustomPacketPayload.Type<>(CircuitCraft.id("component_value_update"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ComponentValueUpdatePayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ComponentValueUpdatePayload::pos,
			ByteBufCodecs.DOUBLE.apply(ByteBufCodecs.list()), ComponentValueUpdatePayload::values,
			ComponentValueUpdatePayload::new
	);

	@Override
	public CustomPacketPayload.Type<ComponentValueUpdatePayload> type() {
		return TYPE;
	}
}
