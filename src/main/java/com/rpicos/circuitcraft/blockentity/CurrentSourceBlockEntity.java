package com.rpicos.circuitcraft.blockentity;

import com.rpicos.circuitcraft.ModBlockEntities;
import com.rpicos.circuitcraft.sim.Circuit;
import com.rpicos.circuitcraft.sim.CurrentSource;
import com.rpicos.circuitcraft.sim.Waveform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** An ideal independent current source - the dual of {@link PowerSupplyBlockEntity}. No AC
 *  participation ({@code AcStampable} not implemented): a silenced independent current source
 *  is an open circuit, which omitting an AC stamp already gives for free - see
 *  {@link com.rpicos.circuitcraft.sim.CurrentSource}'s own doc comment. */
public class CurrentSourceBlockEntity extends ComponentBlockEntity implements ValueEditable {

	private static final double[] PRESETS_AMPS = {0.1, 0.5, 1, 2};

	private int presetIndex = 1;
	private double currentAmps = PRESETS_AMPS[presetIndex];
	private CurrentSource live;
	private boolean redstonePowered = false;

	public CurrentSourceBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.CURRENT_SOURCE, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS_AMPS.length;
		currentAmps = PRESETS_AMPS[presetIndex];
	}

	@Override
	public List<EditableField> editableFields() {
		return List.of(new EditableField("Current", "A",
				PRESETS_AMPS[0], PRESETS_AMPS[PRESETS_AMPS.length - 1], currentAmps));
	}

	@Override
	public void applyEditedValues(List<Double> values) {
		currentAmps = Math.clamp(values.get(0), PRESETS_AMPS[0], PRESETS_AMPS[PRESETS_AMPS.length - 1]);
	}

	/** Called by {@link com.rpicos.circuitcraft.block.CurrentSourceBlock#neighborChanged}. */
	public void setRedstonePowered(boolean powered) {
		if (redstonePowered != powered) {
			redstonePowered = powered;
			markNetworkDirty();
		}
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		bindNodes(circuit, nodeA, nodeB);
		if (!redstonePowered) {
			live = null;
			return;
		}
		live = new CurrentSource(nodeA, nodeB, Waveform.dc(currentAmps));
		circuit.add(live);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.current();
	}

	@Override
	public String probeSummary() {
		String base = "Current Source " + currentAmps + " A DC";
		return redstonePowered ? base : base + " (off - needs redstone)";
	}
}
