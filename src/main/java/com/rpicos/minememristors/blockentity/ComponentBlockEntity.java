package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** A two-terminal circuit component sitting on one block, wired along its FACING axis. */
public abstract class ComponentBlockEntity extends NetworkBlockEntity implements Probeable {

	private final ProbeHistory history = new ProbeHistory();

	private Circuit circuit;
	private int nodeA = -1;
	private int nodeB = -1;

	protected ComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == getFacing() || direction == getFacing().getOpposite();
	}

	/** Called once per rebuild to add this component's simulation element/source to the live circuit. */
	public abstract void addToCircuit(Circuit circuit, int nodeA, int nodeB);

	protected void bindNodes(Circuit circuit, int nodeA, int nodeB) {
		this.circuit = circuit;
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeA) - circuit.getVoltage(nodeB);
	}

	@Override
	public abstract double probeCurrent();

	/** Short human-readable description of this component's current state, for the probe readout. */
	@Override
	public abstract String probeSummary();

	/** Right-click-without-item interaction: cycle to the next preset value. */
	public abstract void cyclePreset();

	/** What the oscilloscope trace plots for this component - voltage for every normal component,
	 *  but an ammeter overrides this to plot current instead, reusing the same trace/graph machinery. */
	protected double sampleValue() {
		return probeVoltage();
	}

	@Override
	public void recordSample() {
		history.record(sampleValue());
	}

	@Override
	public List<Float> historySnapshot() {
		return history.snapshot();
	}
}
