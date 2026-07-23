package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.AcCircuit;
import com.rpicos.circuitcraft.sim.AcVoltageSource;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.Complex;
import com.rpicos.circuitcraft.sim.VoltageSource;
import com.rpicos.circuitcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** The excitation for an AC (Bode-plot) sweep: a two-terminal component, wired like any other,
 *  whose amplitude and swept frequency range are set entirely through the shift-right-click
 *  value editor (see {@link ValueEditable}) rather than a cycled preset, since a frequency
 *  *range* is not the kind of thing a short fixed preset list represents well. In the regular
 *  transient/DC simulation it contributes nothing but a 0V source (electrically a wire), exactly
 *  like the Ammeter, so a circuit built around it still behaves normally when nobody is actually
 *  running a sweep; its real behavior only appears through the dedicated AC probe. Like the Power
 *  Supply and Function Generator, it needs a redstone signal to actually drive its configured
 *  amplitude during a sweep (see {@link #setRedstonePowered}) - unpowered, it sweeps silently at
 *  0V, the same "needs redstone" convention as those other two sources. */
public class AcSourceBlockEntity extends ComponentBlockEntity implements ValueEditable, AcStampable {

	private static final double MIN_AMPLITUDE_VOLTS = 0.01;
	private static final double MAX_AMPLITUDE_VOLTS = 10;
	private static final double MIN_FREQUENCY_HZ = 0.1;
	private static final double MAX_FREQUENCY_HZ = 10_000_000;

	private double amplitudeVolts = 1.0;
	private double minFrequencyHz = 1.0;
	private double maxFrequencyHz = 10_000_000;
	private VoltageSource live;
	private boolean redstonePowered = false;

	public AcSourceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AC_SOURCE, pos, state);
	}

	@Override
	public void cyclePreset() {
		// No discrete preset cycle - every parameter here (amplitude, and especially the swept
		// frequency range) is set through the value editor instead; see class javadoc.
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(
				new EditableField("Amplitude", "V", MIN_AMPLITUDE_VOLTS, MAX_AMPLITUDE_VOLTS, amplitudeVolts),
				new EditableField("Min frequency", "Hz", MIN_FREQUENCY_HZ, MAX_FREQUENCY_HZ, minFrequencyHz),
				new EditableField("Max frequency", "Hz", MIN_FREQUENCY_HZ, MAX_FREQUENCY_HZ, maxFrequencyHz)
		);
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		amplitudeVolts = Math.clamp(values.get(0), MIN_AMPLITUDE_VOLTS, MAX_AMPLITUDE_VOLTS);
		minFrequencyHz = Math.clamp(values.get(1), MIN_FREQUENCY_HZ, MAX_FREQUENCY_HZ);
		maxFrequencyHz = Math.clamp(values.get(2), MIN_FREQUENCY_HZ, MAX_FREQUENCY_HZ);
	}

	public double amplitudeVolts() {
		return amplitudeVolts;
	}

	/** Called by {@link com.rpicos.circuitcraft.block.AcSourceBlock#neighborChanged} whenever a
	 *  redstone neighbor changes. Unlike {@link PowerSupplyBlockEntity#setRedstonePowered} and its
	 *  Function Generator equivalent, this doesn't touch {@code markNetworkDirty()}: the AC Source
	 *  never contributes anything but a 0V source to the transient/DC circuit (see the class
	 *  javadoc), so a redstone flip here doesn't change that circuit at all - it only gates
	 *  whether {@link #addToAcCircuit} drives a real signal or a silenced one, which the AC
	 *  probe's sweep already re-stamps fresh on every request, with no cached network to invalidate.
	 */
	public void setRedstonePowered(boolean powered) {
		redstonePowered = powered;
	}

	public boolean isRedstonePowered() {
		return redstonePowered;
	}

	/** Guards against a min >= max misconfiguration (each field is independently clamped to the
	 *  same overall range, so nothing stops a player setting them the "wrong" way round) by
	 *  falling back to one decade of sweep above the minimum. */
	public double minFrequencyHz() {
		return minFrequencyHz;
	}

	public double maxFrequencyHz() {
		return maxFrequencyHz > minFrequencyHz ? maxFrequencyHz : minFrequencyHz * 10;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new VoltageSource(nodeA, nodeB, Waveform.dc(0));
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public void addToAcCircuit(AcCircuit circuit, int nodeA, int nodeB) {
		// Same "off means silenced, not driving" behavior as an unpowered Power Supply/Function
		// Generator - unpowered, this contributes nothing more than any other inactive source does
		// during a sweep (see PowerSupplyBlockEntity#addToAcCircuit).
		circuit.add(redstonePowered
				? new AcVoltageSource(nodeA, nodeB, Complex.real(amplitudeVolts))
				: AcVoltageSource.zero(nodeA, nodeB));
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = String.format("AC Source %.2fV, %.1f-%.0f Hz sweep", amplitudeVolts, minFrequencyHz, maxFrequencyHz());
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
