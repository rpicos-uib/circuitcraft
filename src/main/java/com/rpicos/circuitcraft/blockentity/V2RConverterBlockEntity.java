package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The inverse of {@link R2VConverterBlockEntity}: reads the voltage across its up lead (node A)
 *  and down lead (node B, the real 0V reference), decodes it back into A/B redstone strengths
 *  (0-15 each) via {@code V = A*16 + B}, and exposes them to
 *  {@link com.rpicos.circuitcraft.block.V2RConverterBlock#getSignal} to actually emit as redstone
 *  power. Stamps no element into the circuit at all - an ideal voltmeter, infinite input
 *  impedance, never loads down whatever it's reading. */
public class V2RConverterBlockEntity extends ComponentBlockEntity {

	private int outputA = 0;
	private int outputB = 0;

	public V2RConverterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.V2R_CONVERTER, pos, state);
	}

	@Override
	public void cyclePreset() {
		// no adjustable value - it just measures whatever voltage it's wired to
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
		return "V2R Converter: V=%.1f V -> A=%d B=%d".formatted(probeVoltage(), outputA, outputB);
	}

	public int outputA() {
		return outputA;
	}

	public int outputB() {
		return outputB;
	}

	/** Called once per tick by {@link com.rpicos.circuitcraft.network.CircuitNetworkManager}
	 *  right after the circuit solves, so the redstone output reflects the *current* tick's
	 *  voltage rather than lagging a tick behind. Only actually notifies neighbors when the
	 *  decoded output changed, mirroring {@link R2VConverterBlockEntity#setRedstoneInputs}'s same
	 *  change-gating - a live, continuously-varying voltage would otherwise force a redstone
	 *  update broadcast every single tick even when the rounded 0-15 output hasn't moved. */
	public void refreshRedstoneOutput() {
		int vInt = (int) Math.round(Math.clamp(probeVoltage(), 0, 255));
		int a = Math.min(15, vInt / 16);
		int b = Math.clamp(vInt - a * 16, 0, 15);
		if (a != outputA || b != outputB) {
			outputA = a;
			outputB = b;
			if (level != null) {
				level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
			}
		}
	}
}
