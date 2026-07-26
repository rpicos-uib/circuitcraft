package com.rpicos.circuitcraft.sim;

/** A current-controlled voltage source: drives {@code V[output] = transresistance *
 *  I[controlSense]} (referenced against ground - see {@link Vccs}). Combines {@link Cccs}'s
 *  0V control-sensing branch with {@link Vcvs}'s dynamic-waveform output branch: both are
 *  one-tick-lagged for the same underlying reason (a {@code VoltageSource}'s {@code current()}
 *  isn't set until after the tick's solve). */
public final class Ccvs {
	private Ccvs() {
	}

	public static VoltageSource create(VoltageSource controlSense, int nodeOutput, double transresistanceOhms) {
		return new VoltageSource(nodeOutput, 0, t -> transresistanceOhms * controlSense.current());
	}
}
