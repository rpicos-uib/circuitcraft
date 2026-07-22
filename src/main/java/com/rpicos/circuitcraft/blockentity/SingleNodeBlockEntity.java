package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A network participant that occupies exactly one electrical node itself, rather than bridging
 *  two terminals like a {@link ComponentBlockEntity} - Wire and Ground. Conductive on all six
 *  faces, and probeable for its absolute node voltage (as opposed to a component's voltage drop
 *  across two leads). */
public abstract class SingleNodeBlockEntity extends NetworkBlockEntity implements Probeable {

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeId = -1;

	protected SingleNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return true;
	}

	/** Called once per rebuild with this block's resolved node id in the live circuit. */
	public void bindNode(Circuit circuit, int nodeId) {
		this.circuit = circuit;
		this.nodeId = nodeId;
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeId);
	}

	@Override
	public double probeCurrent() {
		// current isn't a single well-defined scalar at an arbitrary node the way it is across a
		// two-terminal component's two leads
		return 0;
	}

	@Override
	public void recordSample() {
		history.record(probeVoltage());
	}

	@Override
	public List<Float> historySnapshot() {
		return history.snapshot();
	}
}
