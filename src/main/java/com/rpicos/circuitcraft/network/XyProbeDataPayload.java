package com.rpicos.circuitcraft.network;

import com.rpicos.circuitcraft.CircuitCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server -> client: both halves of an X-Y (Lissajous-style) oscilloscope channel pair, bundled
 *  into one packet so the client always has a matched set rather than two independently-arriving,
 *  possibly out-of-sync channels. */
public record XyProbeDataPayload(BlockPos xPos, String xSummary, float xVoltage, List<Float> xHistory,
		BlockPos yPos, String ySummary, float yVoltage, List<Float> yHistory) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<XyProbeDataPayload> TYPE =
			new CustomPacketPayload.Type<>(CircuitCraft.id("xy_probe_data"));

	public static final StreamCodec<RegistryFriendlyByteBuf, XyProbeDataPayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, XyProbeDataPayload::xPos,
			ByteBufCodecs.STRING_UTF8, XyProbeDataPayload::xSummary,
			ByteBufCodecs.FLOAT, XyProbeDataPayload::xVoltage,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), XyProbeDataPayload::xHistory,
			BlockPos.STREAM_CODEC, XyProbeDataPayload::yPos,
			ByteBufCodecs.STRING_UTF8, XyProbeDataPayload::ySummary,
			ByteBufCodecs.FLOAT, XyProbeDataPayload::yVoltage,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), XyProbeDataPayload::yHistory,
			XyProbeDataPayload::new
	);

	@Override
	public CustomPacketPayload.Type<XyProbeDataPayload> type() {
		return TYPE;
	}
}
