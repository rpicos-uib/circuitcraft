package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.VoltmeterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** An ideal voltmeter - the voltage-domain dual of {@link AmmeterBlock}. Unlike the ammeter,
 *  which is a deliberate 0V short so it can sit *in series* and read the current through it,
 *  a voltmeter is meant to sit *in parallel*: it stamps nothing into the circuit at all (see
 *  {@link VoltmeterBlockEntity#addToCircuit}), so it never completes a circuit path on its
 *  own the way a Wire or Ammeter would. */
public class VoltmeterBlock extends ComponentBlock {
	public static final MapCodec<VoltmeterBlock> CODEC = simpleCodec(VoltmeterBlock::new);

	public VoltmeterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<VoltmeterBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VoltmeterBlockEntity(pos, state);
	}
}
