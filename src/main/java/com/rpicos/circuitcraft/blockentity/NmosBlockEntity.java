package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Mosfet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** An NMOS transistor - same three-terminal, non-{@link ComponentBlockEntity} shape as
 *  {@link NpnBlockEntity}/{@link com.rpicos.circuitcraft.blockentity.OpAmpBlockEntity}, just
 *  with drain/source/gate in place of collector/emitter/base (drain = FACING, source =
 *  opposite, gate = third face). See {@link com.rpicos.circuitcraft.sim.Mosfet} for the model
 *  and why the threshold-voltage field is always a positive magnitude regardless of polarity. */
public class NmosBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double VTH_MIN = 0.1, VTH_MAX = 5;
	private static final double K_MIN = 1e-4, K_MAX = 0.1;

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeDrain = -1, nodeSource = -1;
	private double thresholdVolts = 1.5;
	private double transconductanceParam = 0.01;
	private Mosfet live;

	public NmosBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.NMOS, pos, state);
	}

	protected NmosBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	public Direction drainFace() {
		return getFacing();
	}

	public Direction sourceFace() {
		return getFacing().getOpposite();
	}

	public Direction gateFace() {
		Direction facing = getFacing();
		return (facing == Direction.UP || facing == Direction.DOWN) ? Direction.NORTH : Direction.UP;
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == drainFace() || direction == sourceFace() || direction == gateFace();
	}

	protected int polarity() {
		return 1;
	}

	protected String deviceName() {
		return "NMOS";
	}

	public void addToCircuit(Circuit circuit, int nodeGate, int nodeDrain, int nodeSource) {
		this.circuit = circuit;
		this.nodeDrain = nodeDrain;
		this.nodeSource = nodeSource;
		live = new Mosfet(nodeGate, nodeDrain, nodeSource, thresholdVolts, transconductanceParam, polarity());
		circuit.add(live);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(
				new EditableField("Threshold voltage", "V", VTH_MIN, VTH_MAX, thresholdVolts),
				new EditableField("Transconductance k", "A/V²", K_MIN, K_MAX, transconductanceParam));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		thresholdVolts = Math.clamp(values.get(0), VTH_MIN, VTH_MAX);
		transconductanceParam = Math.clamp(values.get(1), K_MIN, K_MAX);
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeDrain) - circuit.getVoltage(nodeSource);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.drainCurrentAt(circuit.getVoltage(live.gate) - circuit.getVoltage(live.source));
	}

	@Override
	public String probeSummary() {
		return "%s Vth=%.2f V k=%.4f, Vds=%.3f V, Id=%.4f A".formatted(
				deviceName(), thresholdVolts, transconductanceParam, probeVoltage(), probeCurrent());
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
