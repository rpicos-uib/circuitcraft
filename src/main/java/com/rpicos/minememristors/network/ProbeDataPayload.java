package com.rpicos.minememristors.network;

import com.rpicos.minememristors.MineMemristors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/** Server -> client: the probed component's live readout and recent voltage history.
 *  {@code willBeReplacedNext} is true when this channel is the oldest of a player's full
 *  3-channel pin list - i.e. the one a 4th pin would evict - so the client can highlight it. */
public record ProbeDataPayload(BlockPos pos, String summary, float voltage, float current,
		List<Float> history, boolean willBeReplacedNext) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ProbeDataPayload> TYPE =
			new CustomPacketPayload.Type<>(MineMemristors.id("probe_data"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ProbeDataPayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ProbeDataPayload::pos,
			ByteBufCodecs.STRING_UTF8, ProbeDataPayload::summary,
			ByteBufCodecs.FLOAT, ProbeDataPayload::voltage,
			ByteBufCodecs.FLOAT, ProbeDataPayload::current,
			ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), ProbeDataPayload::history,
			ByteBufCodecs.BOOL, ProbeDataPayload::willBeReplacedNext,
			ProbeDataPayload::new
	);

	@Override
	public CustomPacketPayload.Type<ProbeDataPayload> type() {
		return TYPE;
	}
}
