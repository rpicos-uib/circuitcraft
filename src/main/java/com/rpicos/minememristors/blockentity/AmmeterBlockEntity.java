package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import com.rpicos.minememristors.sim.Circuit;
import com.rpicos.minememristors.sim.VoltageSource;
import com.rpicos.minememristors.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** A 0V voltage source in series - electrically an ideal wire, giving an exact branch-current
 *  reading with no series resistance to distort the circuit it's measuring. */
public class AmmeterBlockEntity extends ComponentBlockEntity {

	private VoltageSource live;

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
	public double probeCurrent() {
		return live == null ? 0 : live.current();
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
