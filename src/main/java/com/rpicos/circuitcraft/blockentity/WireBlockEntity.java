package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WireBlockEntity extends SingleNodeBlockEntity {

	public WireBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WIRE, pos, state);
	}

	@Override
	public String probeSummary() {
		return String.format("Wire node: %.2f V", probeVoltage());
	}
}
