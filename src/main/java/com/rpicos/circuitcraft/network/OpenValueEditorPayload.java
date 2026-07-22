package com.rpicos.circuitcraft.network;

import com.rpicos.circuitcraft.CircuitCraft;
import com.rpicos.circuitcraft.blockentity.EditableField;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server -> client: opens the value-editor screen (the same interaction spirit as editing a
 *  sign) for the component at {@code pos}, pre-filled with its current {@link EditableField}s. */
public record OpenValueEditorPayload(BlockPos pos, List<EditableField> fields) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<OpenValueEditorPayload> TYPE =
			new CustomPacketPayload.Type<>(CircuitCraft.id("open_value_editor"));

	public static final StreamCodec<RegistryFriendlyByteBuf, OpenValueEditorPayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, OpenValueEditorPayload::pos,
			EditableField.STREAM_CODEC.apply(ByteBufCodecs.list()), OpenValueEditorPayload::fields,
			OpenValueEditorPayload::new
	);

	@Override
	public CustomPacketPayload.Type<OpenValueEditorPayload> type() {
		return TYPE;
	}
}
