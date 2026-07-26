package com.rpicos.circuitcraft.sim;

/** An ideal independent current source: unlike {@link VoltageSource}, this needs no branch-
 *  current unknown at all - a fixed (or time-varying) current is simply injected straight into
 *  the RHS vector at its two nodes, exactly like {@link Diode}'s own equivalent-current term,
 *  just without an accompanying conductance since a current source has none. Deliberately not
 *  an {@code AcStampable}: an independent source silenced for a small-signal sweep should
 *  become an *open* circuit (zero current, infinite impedance), which is exactly what omitting
 *  an AC stamp already achieves for free - unlike an independent voltage source, whose silenced
 *  AC equivalent is a short (see {@code AcVoltageSource.zero}), not an open. */
public class CurrentSource implements Element {
	public final int a, b;
	public Waveform waveform;

	private double lastCurrent;

	public CurrentSource(int a, int b, Waveform waveform) {
		this.a = a;
		this.b = b;
		this.waveform = waveform;
	}

	/** Current this source is currently driving from a to b. */
	public double current() {
		return lastCurrent;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		lastCurrent = waveform.valueAt(circuit.time() + dt);
		circuit.stampCurrentSource(z, a, b, lastCurrent);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
	}
}
