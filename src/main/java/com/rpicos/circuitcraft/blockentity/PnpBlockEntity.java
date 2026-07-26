package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A PNP transistor - the exact same geometry and {@link com.rpicos.circuitcraft.sim.Bjt} model
 *  as {@link NpnBlockEntity}, just mirrored (base-emitter junction conducts emitter-to-base
 *  instead of base-to-emitter, collector current flows emitter-to-collector instead) via
 *  {@code Bjt}'s own {@code polarity} parameter - see {@link NpnBlockEntity} for everything
 *  else, including why collector/emitter/base map to FACING/opposite/third-face. */
public class PnpBlockEntity extends NpnBlockEntity {

	public PnpBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PNP, pos, state);
	}

	@Override
	protected int polarity() {
		return -1;
	}

	@Override
	protected String deviceName() {
		return "PNP";
	}
}
