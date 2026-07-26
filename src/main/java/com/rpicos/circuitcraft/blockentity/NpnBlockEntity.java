package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Bjt;
import com.rpicos.circuitcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/** An NPN transistor: three electrical terminals, so - exactly like {@link OpAmpBlockEntity} -
 *  it does not fit {@link ComponentBlockEntity}'s two-lead assumption and is wired up directly
 *  here instead, reusing the same front/back/third-face convention. Collector is the FACING
 *  face (the "output" analog), emitter is the opposite face, and base is the block's up face -
 *  or, if the block itself is oriented vertically, its north face instead. See {@link
 *  com.rpicos.circuitcraft.sim.Bjt} for the model itself and {@code beta}'s meaning. */
public class NpnBlockEntity extends NetworkBlockEntity implements Probeable, ValueEditable {

	private static final double BETA_MIN = 5, BETA_MAX = 500;
	private static final double SATURATION_CURRENT_AMPS = 1e-12;
	private static final double IDEALITY_FACTOR = 1.0;

	private final ProbeHistory history = new ProbeHistory();
	private Circuit circuit;
	private int nodeCollector = -1, nodeEmitter = -1;
	private double beta = 100;
	private Bjt live;

	public NpnBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.NPN, pos, state);
	}

	/** Lets {@link PnpBlockEntity} extend this class directly (overriding only {@link
	 *  #polarity()}/{@link #deviceName()}) while still registering under its own, distinct
	 *  {@link BlockEntityType} instead of NPN's. */
	protected NpnBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public Direction getFacing() {
		return getBlockState().getValue(BlockStateProperties.FACING);
	}

	public Direction collectorFace() {
		return getFacing();
	}

	public Direction emitterFace() {
		return getFacing().getOpposite();
	}

	public Direction baseFace() {
		Direction facing = getFacing();
		return (facing == Direction.UP || facing == Direction.DOWN) ? Direction.NORTH : Direction.UP;
	}

	@Override
	public boolean isConductiveTowards(Direction direction) {
		return direction == collectorFace() || direction == emitterFace() || direction == baseFace();
	}

	/** The polarity this device's underlying {@link Bjt} model uses - {@code +1} for NPN.
	 *  Overridden by {@link PnpBlockEntity} rather than duplicating this whole class. */
	protected int polarity() {
		return 1;
	}

	protected String deviceName() {
		return "NPN";
	}

	public void addToCircuit(Circuit circuit, int nodeBase, int nodeCollector, int nodeEmitter) {
		this.circuit = circuit;
		this.nodeCollector = nodeCollector;
		this.nodeEmitter = nodeEmitter;
		live = new Bjt(nodeBase, nodeCollector, nodeEmitter, beta, SATURATION_CURRENT_AMPS, IDEALITY_FACTOR, polarity());
		circuit.add(live);
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Beta (current gain)", "", BETA_MIN, BETA_MAX, beta));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		beta = Math.clamp(values.get(0), BETA_MIN, BETA_MAX);
	}

	@Override
	public double probeVoltage() {
		return circuit == null ? 0 : circuit.getVoltage(nodeCollector) - circuit.getVoltage(nodeEmitter);
	}

	@Override
	public double probeCurrent() {
		// Collector current, the transistor's main "output" quantity - beta times whatever
		// base current the base-emitter junction is presently conducting.
		return live == null ? 0 : beta * live.baseCurrentAt(circuit.getVoltage(live.base) - circuit.getVoltage(live.emitter));
	}

	@Override
	public String probeSummary() {
		return "%s beta=%.0f, Vce=%.3f V, Ic=%.4f A".formatted(deviceName(), beta, probeVoltage(), probeCurrent());
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
