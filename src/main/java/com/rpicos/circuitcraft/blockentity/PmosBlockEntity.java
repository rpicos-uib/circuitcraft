package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A PMOS transistor - same geometry and {@link com.rpicos.circuitcraft.sim.Mosfet} model as
 *  {@link NmosBlockEntity}, mirrored via {@code Mosfet}'s own {@code polarity} parameter. */
public class PmosBlockEntity extends NmosBlockEntity {

	public PmosBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PMOS, pos, state);
	}

	@Override
	protected int polarity() {
		return -1;
	}

	@Override
	protected String deviceName() {
		return "PMOS";
	}
}
