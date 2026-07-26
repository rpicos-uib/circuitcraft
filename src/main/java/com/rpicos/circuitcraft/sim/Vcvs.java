package com.rpicos.circuitcraft.sim;

/** A voltage-controlled voltage source: drives {@code V[output] = gain * V[control]}, both
 *  referenced against the real 0V ground node (see {@link Vccs}'s note on why). Built as a
 *  plain {@link VoltageSource} whose waveform closes over the live {@link Circuit} and control
 *  node instead of holding a fixed value, so it reuses {@code VoltageSource}'s own
 *  branch-current-unknown machinery completely unmodified - no changes to {@link Circuit}
 *  itself. Because node voltages aren't updated until *after* each tick's solve, evaluating
 *  {@code circuit.getVoltage(nodeControl)} while this waveform is invoked (which happens
 *  *during* that same tick's matrix construction, before the solve) necessarily reads the
 *  *previous* tick's converged voltage - the same deliberate one-tick lag {@link Cccs} accepts
 *  for the same reason. */
public final class Vcvs {
	private Vcvs() {
	}

	public static VoltageSource create(Circuit circuit, int nodeControl, int nodeOutput, double gain) {
		return new VoltageSource(nodeOutput, 0, t -> gain * circuit.getVoltage(nodeControl));
	}
}
