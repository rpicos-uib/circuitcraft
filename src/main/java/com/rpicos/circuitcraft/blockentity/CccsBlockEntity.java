package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Cccs;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.VoltageSource;
import com.rpicos.circuitcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A current-controlled current source: {@code I[output] = gain * I[control]}. Needs a fourth
 *  electrical face compared to {@link VcvsBlockEntity}/{@link VccsBlockEntity}, since sensing a
 *  *current* (rather than a voltage) needs two terminals for it to flow through in series -
 *  north = control-in, south = control-out (an ideal 0V "ammeter" branch, the exact same trick
 *  {@link AmmeterBlockEntity} uses), down = ground/required placement, up = output. See
 *  {@link com.rpicos.circuitcraft.sim.Cccs} for the model and its one-tick-lagged
 *  simplification. */
public class CccsBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double GAIN_MIN = 0.1, GAIN_MAX = 100;

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeOutput = -1;
	private double currentGain = 10;
	private VoltageSource controlSense;
	private Cccs live;

	public CccsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CCCS, pos, state);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == Direction.DOWN || direction == Direction.UP
				|| direction == Direction.NORTH || direction == Direction.SOUTH;
	}

	public void addToCircuit(Circuit circuit, int nodeControlIn, int nodeControlOut, int nodeOutput) {
		this.circuit = circuit;
		this.nodeOutput = nodeOutput;
		controlSense = new VoltageSource(nodeControlIn, nodeControlOut, Waveform.dc(0));
		circuit.add(controlSense);
		live = new Cccs(nodeOutput, controlSense, currentGain);
		circuit.add(live);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Current gain", "A/A", GAIN_MIN, GAIN_MAX, currentGain));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		currentGain = Math.clamp(values.get(0), GAIN_MIN, GAIN_MAX);
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeOutput);
	}

	@Override
	public double probeCurrent() {
		return controlSense == null ? 0 : currentGain * controlSense.current();
	}

	@Override
	public String probeSummary() {
		double iControl = controlSense == null ? 0 : controlSense.current();
		return "CCCS gain=%.2f, Iin=%.4f A, Iout=%.4f A".formatted(currentGain, iControl, probeCurrent());
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
