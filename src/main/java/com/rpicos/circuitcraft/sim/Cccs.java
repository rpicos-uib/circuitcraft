package com.rpicos.circuitcraft.sim;

/** A current-controlled current source: injects {@code gain * I[controlSense]} into the output
 *  node (referenced against ground - see {@link Vccs}'s same note on why). The controlling
 *  current comes from a {@link VoltageSource} at 0V wired in series wherever the controlling
 *  current is meant to flow - an "ammeter" sensing branch, the exact same trick
 *  {@code AmmeterBlockEntity} already uses. That branch's own solved current isn't known until
 *  *after* {@link Circuit#step}'s solve for the tick (a {@code VoltageSource}'s
 *  {@code current()} is only set post-solve), so reading it here, during this tick's own
 *  {@link #stamp}, necessarily returns the *previous* tick's converged value - a deliberate
 *  one-tick-lagged simplification, chosen over adding a new exact-but-simultaneous
 *  branch-unknown mechanism to {@link Circuit} itself for a single dependent-source type. */
public class Cccs implements Element {
	public final int nodeOutput;
	public final VoltageSource controlSense;
	public double currentGain;

	public Cccs(int nodeOutput, VoltageSource controlSense, double currentGain) {
		this.nodeOutput = nodeOutput;
		this.controlSense = controlSense;
		this.currentGain = currentGain;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		circuit.stampCurrentSource(z, nodeOutput, 0, currentGain * controlSense.current());
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
	}
}
