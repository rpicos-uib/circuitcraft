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

/** A 0V voltage source in series - electrically an ideal wire, giving an exact branch-current
 *  reading with no series resistance to distort the circuit it's measuring. */
public class AmmeterBlockEntity extends ComponentBlockEntity implements AcStampable {

	private VoltageSource live;
	private AcVoltageSource liveAc;

	public AmmeterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.AMMETER, pos, state);
	}

	@Override
	public void cyclePreset() {
		// no adjustable value - an ammeter just measures whatever current flows through it
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		live = new VoltageSource(nodeA, nodeB, Waveform.dc(0));
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public void addToAcCircuit(AcCircuit circuit, int nodeA, int nodeB) {
		// Still a 0V source in AC, exactly as in the transient case - an ideal ammeter is already
		// electrically a wire, so there's no separate "silenced" case to handle here. Kept as a
		// field (mirroring `live` above) so acCurrent() can read its solved branch current back
		// after the caller's AcCircuit#solve - the voltage across an ideal ammeter is identically
		// zero at every frequency by construction, so an AC probe pinned on it must read current,
		// not voltage, or it would always show a flat, meaningless trace.
		liveAc = AcVoltageSource.zero(nodeA, nodeB);
		circuit.add(liveAc);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	/** The AC probe's counterpart to {@link #probeCurrent()} - see {@link #addToAcCircuit}. */
	public Complex acCurrent() {
		return liveAc == null ? Complex.ZERO : liveAc.current();
	}

	@Override
	public String probeSummary() {
		return String.format("Ammeter %.4f A", probeCurrent());
	}

	@Override
	protected double sampleValue() {
		// the oscilloscope trace plots current here, not voltage (which is always ~0 across an
		// ideal ammeter and wouldn't be a useful signal to look at)
		return probeCurrent();
	}
}
