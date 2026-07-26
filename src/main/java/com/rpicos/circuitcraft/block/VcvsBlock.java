package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.VcvsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Reuses {@link GroundedComponentBlock}'s pinned-FACING/must-sit-on-Ground handling: a VCVS's
 *  north (control) and up (output) faces are both referenced against that same required Ground
 *  block, exactly like {@code R2VConverterBlock}. */
public class VcvsBlock extends GroundedComponentBlock {
	public static final MapCodec<VcvsBlock> CODEC = simpleCodec(VcvsBlock::new);

	public VcvsBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<VcvsBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VcvsBlockEntity(pos, state);
	}
}
