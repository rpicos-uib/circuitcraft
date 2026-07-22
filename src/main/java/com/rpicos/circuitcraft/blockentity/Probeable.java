package com.rpicos.circuitcraft.blockentity;

import java.util.List;

/** Anything the Probe item can pin as an oscilloscope channel: a two-terminal component (voltage
 *  drop across its leads) or a single circuit node like Wire/Ground (absolute voltage at that
 *  point, relative to whatever Ground block - if any - anchors the network's reference). */
public interface Probeable {
	double probeVoltage();

	double probeCurrent();

	String probeSummary();

	List<Float> historySnapshot();

	/** Called once per tick (from the network manager, after the circuit solve) to append a scope sample. */
	void recordSample();
}
