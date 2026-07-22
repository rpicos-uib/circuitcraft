package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.network.CircuitNetworkManager;
import com.rpicos.circuitcraft.network.NetworkParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Base for anything that should participate in the world's circuit graph (wires and components). */
public abstract class NetworkBlockEntity extends BlockEntity implements NetworkParticipant {

	protected NetworkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (level instanceof ServerLevel serverLevel) {
			CircuitNetworkManager.forLevel(serverLevel).register(getBlockPos(), this);
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (level instanceof ServerLevel serverLevel) {
			CircuitNetworkManager.forLevel(serverLevel).unregister(getBlockPos());
		}
	}

	/** Call after changing a component's value (e.g. cycling a resistance preset) so the next
	 *  tick rebuilds the circuit with the new value instead of waiting for a topology change. */
	public void markNetworkDirty() {
		if (level instanceof ServerLevel serverLevel) {
			CircuitNetworkManager.forLevel(serverLevel).markDirty();
		}
	}
}
