package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.AcCircuit;
import com.rpicos.circuitcraft.sim.AcVoltageSource;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.VoltageSource;
import com.rpicos.circuitcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Stamps V = redstoneA*16 + redstoneB as a plain DC voltage source between its up lead (node A)
 *  and its down lead (node B, always the real 0V reference - see
 *  {@link com.rpicos.circuitcraft.block.GroundedComponentBlock}). {@code redstoneA}/{@code
 *  redstoneB} are pushed in by {@link com.rpicos.circuitcraft.block.R2VConverterBlock#neighborChanged}
 *  whenever a redstone neighbor changes, not read live during a circuit rebuild - a rebuild can
 *  happen for reasons unrelated to redstone (e.g. a neighboring component's own value changing),
 *  so the last-known signal strengths need to already be cached here. Deliberately does *not*
 *  read its neighbors' redstone state from {@code setLevel} on attach (unlike a first instinct
 *  might suggest, to avoid a freshly-placed block starting at a stale 0/0 reading) - querying
 *  neighboring block state that early, while this chunk's neighbors may still be mid-load, is a
 *  known way to trigger expensive/cascading chunk-loading from inside block-entity attachment.
 *  {@link com.rpicos.circuitcraft.block.PowerSupplyBlock} already gets away with the same
 *  neighborChanged-only approach for its own redstone gating, so this mirrors a proven pattern
 *  rather than reaching for extra startup-time correctness that isn't actually free. */
public class R2VConverterBlockEntity extends ComponentBlockEntity implements AcStampable {

	private int redstoneA = 0;
	private int redstoneB = 0;
	private VoltageSource live;

	public R2VConverterBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.R2V_CONVERTER, pos, state);
	}

	public void setRedstoneInputs(int a, int b) {
		if (a != redstoneA || b != redstoneB) {
			redstoneA = a;
			redstoneB = b;
			markNetworkDirty();
		}
	}

	/** The multiplier is 16, not 15 - redstone strength maxes out at 15, but there are 16
	 *  possible values per digit (0-15 inclusive), and a positional encoding's base is the
	 *  count of possible digit values, not the largest one (exactly like base-10 place value
	 *  uses 10, not 9, the largest decimal digit). Using 15 here would make the encoding lossy:
	 *  A=0,B=15 and A=1,B=0 would both produce V=15, an ambiguity {@link
	 *  V2RConverterBlockEntity}'s decoder could never resolve correctly for both inputs. */
	private double voltageVolts() {
		return redstoneA * 16.0 + redstoneB;
	}

	@Override
	public void cyclePreset() {
		// no adjustable value - the output is entirely determined by the redstone inputs
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		bindNodes(circuit, nodeA, nodeB);
		live = new VoltageSource(nodeA, nodeB, Waveform.dc(voltageVolts()));
		circuit.add(live);
	}

	@Override
	public void addToAcCircuit(AcCircuit circuit, int nodeA, int nodeB) {
		// Same convention as every other independent DC source: silenced to a 0V small-signal
		// source during an AC sweep, regardless of its own current redstone-derived value.
		circuit.add(AcVoltageSource.zero(nodeA, nodeB));
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		return "R2V Converter: A=%d B=%d -> V=%.1f V".formatted(redstoneA, redstoneB, voltageVolts());
	}
}
