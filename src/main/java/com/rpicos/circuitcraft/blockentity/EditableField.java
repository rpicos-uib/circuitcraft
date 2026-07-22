package com.rpicos.circuitcraft.blockentity;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import io.netty.buffer.ByteBuf;

/** One numeric parameter exposed by a {@link ValueEditable} component to the value-editor
 *  screen: a label, a display unit, and the same min/max range already used to bound that
 *  component's existing right-click preset cycle - the editor never accepts a value outside
 *  the range the component could already reach some other way. */
public record EditableField(String label, String unit, double min, double max, double current) {

	public static final StreamCodec<ByteBuf, EditableField> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, EditableField::label,
			ByteBufCodecs.STRING_UTF8, EditableField::unit,
			ByteBufCodecs.DOUBLE, EditableField::min,
			ByteBufCodecs.DOUBLE, EditableField::max,
			ByteBufCodecs.DOUBLE, EditableField::current,
			EditableField::new
	);
}
