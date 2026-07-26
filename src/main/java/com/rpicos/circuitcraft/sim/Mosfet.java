package com.rpicos.circuitcraft.sim;

/** Simplest square-law MOSFET model, saturation region only (no triode/ohmic region, no
 *  channel-length modulation): drain current magnitude {@code Id = k * (Vov)^2} where
 *  {@code Vov = polarity*(Vgate-Vsource) - thresholdVolts} is the overdrive voltage, 0
 *  (cutoff) when {@code Vov <= 0}. The gate draws no DC current at all (an ideal, infinite
 *  gate impedance) - it only ever appears as the controlling voltage of a transconductance
 *  term, never as a current path, matching a real MOSFET's insulated gate. Linearized each
 *  tick about the previous tick's Vgs into a companion transconductance-plus-equivalent-
 *  current pair, the same spirit {@link Diode} already uses for its own nonlinear I-V
 *  relation - not a full Newton-Raphson iteration within a single tick.
 *
 *  <p>{@code polarity} is {@code +1} for an NMOS - conducts (current flows drain to source)
 *  when {@code Vgs} exceeds a positive threshold - or {@code -1} for a PMOS - conducts
 *  (current flows source to drain) when {@code Vgs} falls more than a threshold *below* zero.
 *  Deliberately takes {@code thresholdVolts} as a plain positive magnitude for both polarities
 *  (folding the sign difference entirely into {@code polarity} instead), so the value editor
 *  never asks a player to remember "PMOS thresholds are negative" - one of this model's
 *  simplifications, not a physical claim that real depletion-mode devices work this way. */
public class Mosfet implements Element {
	public final int gate, drain, source;
	public double thresholdVolts;
	public double transconductanceParam; // k, in A/V^2
	public final int polarity;

	private double vgsPrev = 0;

	public Mosfet(int gate, int drain, int source, double thresholdVolts, double transconductanceParam, int polarity) {
		this.gate = gate;
		this.drain = drain;
		this.source = source;
		this.thresholdVolts = thresholdVolts;
		this.transconductanceParam = transconductanceParam;
		this.polarity = polarity;
	}

	private double overdrive(double vgs) {
		return polarity * vgs - thresholdVolts;
	}

	/** Drain-to-source current magnitude at an arbitrary Vgs - used for the probe readout
	 *  (unclamped, exact), as opposed to the linearization point used for stamping. */
	public double drainCurrentAt(double vgs) {
		double vov = overdrive(vgs);
		return vov > 0 ? transconductanceParam * vov * vov : 0;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		double vov = overdrive(vgsPrev);
		double id0 = vov > 0 ? transconductanceParam * vov * vov : 0;
		double gmMagnitude = vov > 0 ? 2 * transconductanceParam * vov : 0;
		double gmSigned = gmMagnitude * polarity;
		double ieq = id0 - gmSigned * vgsPrev;

		int high = polarity > 0 ? drain : source;
		int low = polarity > 0 ? source : drain;
		circuit.stampTransconductance(mat, high, low, gate, source, gmSigned);
		circuit.stampCurrentSource(z, high, low, ieq);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		vgsPrev = circuit.getVoltage(gate) - circuit.getVoltage(source);
	}
}
