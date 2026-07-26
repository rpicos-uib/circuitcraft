package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Vcvs;
import com.rpicos.circuitcraft.sim.VoltageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A voltage-controlled voltage source: {@code V[output] = gain * V[control]}. Always placed
 *  directly on a Ground block (see {@code GroundedComponentBlock}) - both nodes are referenced
 *  against that real 0V node rather than a floating differential pair, which is what lets this
 *  component get away with only three electrical faces (down = ground/required placement,
 *  north = control input, up = output) instead of the four a fully general controlled source
 *  would otherwise need. Since {@code GroundedComponentBlock} pins FACING permanently to UP,
 *  these three faces are hardcoded absolute directions, not resolved via {@code getFacing()}
 *  the way a normal rotatable component's leads would be. See {@link
 *  com.rpicos.circuitcraft.sim.Vcvs} for the model and its one-tick-lagged simplification. */
public class VcvsBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double GAIN_MIN = 0.1, GAIN_MAX = 100;

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeControl = -1, nodeOutput = -1;
	private double gain = 2;
	private VoltageSource live;

	public VcvsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.VCVS, pos, state);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == Direction.DOWN || direction == Direction.UP || direction == Direction.NORTH;
	}

	public void addToCircuit(Circuit circuit, int nodeControl, int nodeOutput) {
		this.circuit = circuit;
		this.nodeControl = nodeControl;
		this.nodeOutput = nodeOutput;
		live = Vcvs.create(circuit, nodeControl, nodeOutput, gain);
		circuit.add(live);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Gain", "V/V", GAIN_MIN, GAIN_MAX, gain));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		gain = Math.clamp(values.get(0), GAIN_MIN, GAIN_MAX);
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
		double vControl = circuit == null ? 0 : circuit.getVoltage(nodeControl);
		return "VCVS gain=%.2f, Vin=%.3f V, Vout=%.3f V".formatted(gain, vControl, probeVoltage());
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
