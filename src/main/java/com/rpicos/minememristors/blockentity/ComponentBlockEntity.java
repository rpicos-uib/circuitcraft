package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;

/** A two-terminal circuit component sitting on one block, wired along its FACING axis. */
public abstract class ComponentBlockEntity extends NetworkBlockEntity {

	private static final int HISTORY_SIZE = 200;

	private Circuit circuit;
	private int nodeA = -1;
	private int nodeB = -1;

	private final float[] history = new float[HISTORY_SIZE];
	private int historyWriteIndex = 0;
	private int historyCount = 0;

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

	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeA) - circuit.getVoltage(nodeB);
	}

	public abstract double probeCurrent();

	/** Short human-readable description of this component's current state, for the probe readout. */
	public abstract String probeSummary();

	/** Right-click-without-item interaction: cycle to the next preset value. */
	public abstract void cyclePreset();

	/** Called once per tick (from the network manager, after the circuit solve) to append a scope sample. */
	public void recordSample() {
		history[historyWriteIndex] = (float) probeVoltage();
		historyWriteIndex = (historyWriteIndex + 1) % HISTORY_SIZE;
		historyCount = Math.min(historyCount + 1, HISTORY_SIZE);
	}

	/** Oldest-to-newest snapshot of recent voltage samples, for the oscilloscope. */
	public List<Float> historySnapshot() {
		List<Float> out = new ArrayList<>(historyCount);
		int start = (historyWriteIndex - historyCount + HISTORY_SIZE) % HISTORY_SIZE;
		for (int i = 0; i < historyCount; i++) {
			out.add(history[(start + i) % HISTORY_SIZE]);
		}
		return out;
	}
}
