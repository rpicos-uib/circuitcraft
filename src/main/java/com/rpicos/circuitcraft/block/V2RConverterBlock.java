package com.rpicos.circuitcraft.block;

import com.mojang.serialization.MapCodec;
import com.rpicos.circuitcraft.blockentity.V2RConverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** Voltage-to-Redstone converter. Reads the voltage on its up lead (referenced against the
 *  Ground block it must be placed directly on, its down lead, enforced by
 *  {@link GroundedComponentBlock}) and decodes it back into two 0-15 redstone outputs - North =
 *  A, South = B - the exact inverse of {@link R2VConverterBlock}'s V = A*15 + B encoding. Draws
 *  no current itself (an ideal voltmeter), so it never loads down whatever it's reading. */
public class V2RConverterBlock extends GroundedComponentBlock {
	public static final MapCodec<V2RConverterBlock> CODEC = simpleCodec(V2RConverterBlock::new);

	public V2RConverterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public MapCodec<V2RConverterBlock> codec() {
		return CODEC;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new V2RConverterBlockEntity(pos, state);
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	/** A neighbor querying this block passes the compass direction from itself to us, which is
	 *  the *opposite* of the face of ours it's actually touching - e.g. a block touching our
	 *  north face (and therefore reading the A output) is reached via {@code pos.north()}, so it
	 *  queries us with {@code direction = SOUTH}. Mirrors vanilla DiodeBlock's own
	 *  {@code direction == FACING} check for exactly the same reason. */
	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		if (!(level.getBlockEntity(pos) instanceof V2RConverterBlockEntity v2r)) {
			return 0;
		}
		if (direction == Direction.SOUTH) {
			return v2r.outputA();
		}
		if (direction == Direction.NORTH) {
			return v2r.outputB();
		}
		return 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return getSignal(state, level, pos, direction);
	}
}
