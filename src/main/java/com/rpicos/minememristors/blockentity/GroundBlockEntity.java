package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Anchors whatever network it touches to the solver's real node 0 (see
 *  {@link com.rpicos.minememristors.network.CircuitNetworkManager}), giving that network a
 *  meaningful 0V reference point - so a Wire's "voltage at this node" reading actually means
 *  something, instead of being relative to an arbitrary internal reference. */
public class GroundBlockEntity extends SingleNodeBlockEntity {

	public GroundBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.GROUND, pos, state);
	}

	@Override
	public String probeSummary() {
		return "Ground (0V reference)";
	}
}
