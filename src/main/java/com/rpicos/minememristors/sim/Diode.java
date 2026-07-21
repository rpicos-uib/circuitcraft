package com.rpicos.minememristors.sim;

/** A diode, linearized about the previous tick's terminal voltage into a companion
 *  conductance-plus-Norton-source pair - the same "reuse last tick's converged state" spirit
 *  the reactive elements' trapezoidal companion models already use, rather than iterating a
 *  full Newton-Raphson solve within a single tick. */
public class Diode implements Element {
	private static final double THERMAL_VOLTAGE = 0.02585; // kT/q at ~300 K
	// Clamps the linearization point, not the actual solved voltage, so a large forward swing
	// between ticks can't send exp() to infinity.
	private static final double MAX_LINEARIZATION_VOLTS = 0.85;

	public final int a, b;
	public double saturationCurrentAmps;
	public double idealityFactor;

	private double vPrev = 0;

	public Diode(int a, int b, double saturationCurrentAmps, double idealityFactor) {
		this.a = a;
		this.b = b;
		this.saturationCurrentAmps = saturationCurrentAmps;
		this.idealityFactor = idealityFactor;
	}

	/** Shockley diode equation, evaluated exactly (unclamped) at an arbitrary voltage - used for
	 *  the probe readout, as opposed to the clamped linearization point used for stamping. */
	public double currentAt(double v) {
		double vt = idealityFactor * THERMAL_VOLTAGE;
		return saturationCurrentAmps * (Math.exp(v / vt) - 1);
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		double vt = idealityFactor * THERMAL_VOLTAGE;
		double v0 = Math.min(vPrev, MAX_LINEARIZATION_VOLTS);
		double iAtV0 = saturationCurrentAmps * (Math.exp(v0 / vt) - 1);
		double geq = (saturationCurrentAmps / vt) * Math.exp(v0 / vt);
		double ieq = iAtV0 - geq * v0;
		circuit.stampConductance(mat, a, b, geq);
		circuit.stampCurrentSource(z, a, b, ieq);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		vPrev = circuit.getVoltage(a) - circuit.getVoltage(b);
	}
}
