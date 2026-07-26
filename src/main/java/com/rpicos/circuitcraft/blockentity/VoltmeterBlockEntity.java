package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** An ideal voltmeter: infinite input impedance, stamps no element into the circuit at all -
 *  just {@link #bindNodes} so {@link #probeVoltage()} (inherited) reads the drop across its
 *  two leads. Meant to be wired in parallel with whatever it's measuring, unlike
 *  {@link AmmeterBlockEntity}, which is deliberately a 0V short meant to sit in series. */
public class VoltmeterBlockEntity extends ComponentBlockEntity {

	public VoltmeterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.VOLTMETER, pos, state);
	}

	@Override
	public void cyclePreset() {
		// no adjustable value - it just measures whatever voltage it's wired across
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		// an ideal voltmeter draws no current
		return 0;
	}

	@Override
	public String probeSummary() {
		return "Voltmeter %.4f V".formatted(probeVoltage());
	}
}
