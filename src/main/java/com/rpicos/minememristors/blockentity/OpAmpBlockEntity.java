package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import com.rpicos.minememristors.sim.Circuit;
import com.rpicos.minememristors.sim.IdealOpAmp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** An ideal op-amp: three electrical terminals rather than the usual two, so it does not fit
 *  {@link ComponentBlockEntity}'s two-lead assumption and is wired up directly here instead.
 *  Output is the FACING face, the inverting input (V-) is the opposite face (reusing the same
 *  front/back convention every other component uses), and the non-inverting input (V+) is the
 *  block's up face - or, if the block itself is oriented vertically (FACING up/down, so "up"
 *  would coincide with output or V-), its north face instead. */
public class OpAmpBlockEntity extends NetworkBlockEntity implements Probeable {

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeOut = -1;
	private int nodeMinus = -1;
	private int nodePlus = -1;
	private IdealOpAmp live;

	public OpAmpBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.OP_AMP, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	public Direction outputFace() {
		return getFacing();
	}

	public Direction minusFace() {
		return getFacing().getOpposite();
	}

	public Direction plusFace() {
		Direction facing = getFacing();
		return (facing == Direction.UP || facing == Direction.DOWN) ? Direction.NORTH : Direction.UP;
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == outputFace() || direction == minusFace() || direction == plusFace();
	}

	/** Called once per rebuild with this op-amp's three resolved node indices. */
	public void addToCircuit(Circuit circuit, int nodeOut, int nodeMinus, int nodePlus) {
		this.circuit = circuit;
		this.nodeOut = nodeOut;
		this.nodeMinus = nodeMinus;
		this.nodePlus = nodePlus;
		live = new IdealOpAmp(nodePlus, nodeMinus, nodeOut);
		circuit.add(live);
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeOut);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.outputCurrent();
	}

	@Override
	public String probeSummary() {
		return "Ideal Op-Amp";
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
