package com.rpicos.circuitcraft.network;

import net.minecraft.core.Direction;

/** A block position that can carry current: either a wire (conductive on all faces) or a
 *  component (conductive only through its two lead faces). */
public interface NetworkParticipant {
	boolean isConductiveTowards(Direction direction);
}
