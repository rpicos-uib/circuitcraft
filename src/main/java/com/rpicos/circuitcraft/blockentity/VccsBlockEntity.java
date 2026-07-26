package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Vccs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** A voltage-controlled current source: {@code I[output] = gm * V[control]}, injected into the
 *  output node (relative to ground, via the required Ground block below - see
 *  {@link VcvsBlockEntity} for the exact same face/geometry reasoning). Unlike VCVS, this is a
 *  plain linear {@code Element} ({@link com.rpicos.circuitcraft.sim.Vccs}) with no branch-current
 *  unknown and no one-tick lag - the relation is expressed exactly via a matrix coefficient,
 *  solved simultaneously with the rest of the tick. */
public class VccsBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double GM_MIN = 1e-4, GM_MAX = 1;

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeControl = -1, nodeOutput = -1;
	private double transconductanceSiemens = 0.01;
	private Vccs live;

	public VccsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.VCCS, pos, state);
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == Direction.DOWN || direction == Direction.UP || direction == Direction.NORTH;
	}

	public void addToCircuit(Circuit circuit, int nodeControl, int nodeOutput) {
		this.circuit = circuit;
		this.nodeControl = nodeControl;
		this.nodeOutput = nodeOutput;
		live = new Vccs(nodeControl, nodeOutput, transconductanceSiemens);
		circuit.add(live);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Transconductance", "S", GM_MIN, GM_MAX, transconductanceSiemens));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		transconductanceSiemens = Math.clamp(values.get(0), GM_MIN, GM_MAX);
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeOutput);
	}

	@Override
	public double probeCurrent() {
		return circuit == null ? 0 : transconductanceSiemens * circuit.getVoltage(nodeControl);
	}

	@Override
	public String probeSummary() {
		double vControl = circuit == null ? 0 : circuit.getVoltage(nodeControl);
		return "VCCS gm=%.4f S, Vin=%.3f V, Iout=%.4f A".formatted(transconductanceSiemens, vControl, probeCurrent());
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
