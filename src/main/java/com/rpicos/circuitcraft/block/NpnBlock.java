package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.NpnBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Reuses ComponentBlock's FACING placement/rotation/interaction handling as-is, exactly like
 *  {@link OpAmpBlock} does for the same reason: a three-terminal component's block-side
 *  placement logic doesn't depend on how many leads its block entity actually has. */
public class NpnBlock extends ComponentBlock {
	public static final MapCodec<NpnBlock> CODEC = simpleCodec(NpnBlock::new);

	public NpnBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<NpnBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new NpnBlockEntity(pos, state);
	}
}
