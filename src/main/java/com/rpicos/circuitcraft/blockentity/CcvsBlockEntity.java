package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Ccvs;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.VoltageSource;
import com.rpicos.circuitcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A current-controlled voltage source: {@code V[output] = transresistance * I[control]}. Same
 *  four-face geometry as {@link CccsBlockEntity} (its control-sensing branch is identical, an
 *  ideal 0V "ammeter" between north and south); the output side is a driven voltage instead of
 *  an injected current, using {@link com.rpicos.circuitcraft.sim.Ccvs}'s same dynamic-waveform
 *  {@code VoltageSource} trick {@link VcvsBlockEntity} uses. */
public class CcvsBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double GAIN_MIN = 1, GAIN_MAX = 1000;

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeOutput = -1;
	private double transresistanceOhms = 100;
	private VoltageSource controlSense;
	private VoltageSource live;

	public CcvsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CCVS, pos, state);
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
		live = Ccvs.create(controlSense, nodeOutput, transresistanceOhms);
		circuit.add(live);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Transresistance", "Ω", GAIN_MIN, GAIN_MAX, transresistanceOhms));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		transresistanceOhms = Math.clamp(values.get(0), GAIN_MIN, GAIN_MAX);
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeOutput);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		double iControl = controlSense == null ? 0 : controlSense.current();
		return "CCVS gain=%.1f Ω, Iin=%.4f A, Vout=%.3f V".formatted(transresistanceOhms, iControl, probeVoltage());
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
