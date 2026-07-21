package com.rpicos.minememristors.blockentity;

import com.rpicos.minememristors.ModBlockEntities;
import com.rpicos.minememristors.sim.Circuit;
import com.rpicos.minememristors.sim.Diode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The lead facing the direction the player looked when placing it (terminal A) is the anode;
 *  the opposite lead (terminal B) is the cathode - current flows readily from anode to cathode
 *  once the forward voltage is exceeded, and is (almost) blocked in reverse. */
public class DiodeBlockEntity extends ComponentBlockEntity {

	private record Preset(String name, double saturationCurrentAmps, double idealityFactor) {
	}

	// Saturation currents/ideality factors tuned to give roughly the textbook forward-voltage
	// figure for each diode family at a typical few-milliamp forward current.
	private static final Preset[] PRESETS = {
			new Preset("silicon (~0.7V)", 1e-14, 1.0),
			new Preset("germanium (~0.3V)", 1e-7, 1.0),
			new Preset("LED red (~2V)", 1e-20, 2.0),
	};

	private int presetIndex = 0;
	private Diode live;

	public DiodeBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.DIODE, pos, state);
	}

	@Override
	public void cyclePreset() {
		presetIndex = (presetIndex + 1) % PRESETS.length;
	}

	@Override
	public void addToCircuit(Circuit circuit, int nodeA, int nodeB) {
		Preset preset = PRESETS[presetIndex];
		live = new Diode(nodeA, nodeB, preset.saturationCurrentAmps(), preset.idealityFactor());
		circuit.add(live);
		bindNodes(circuit, nodeA, nodeB);
	}

	@Override
	public double probeCurrent() {
		return live == null ? 0 : live.currentAt(probeVoltage());
	}

	@Override
	public String probeSummary() {
		return "Diode: " + PRESETS[presetIndex].name();
	}
}
