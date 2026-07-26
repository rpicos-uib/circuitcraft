package com.rpicos.circuitcraft.sim;

/** The Shockley diode equation's per-tick companion linearization (a conductance plus an
 *  equivalent current about a fixed operating-point voltage), factored out of {@link Diode} so
 *  {@link Bjt}'s base-emitter junction - which behaves exactly like an ordinary diode - can
 *  reuse the identical math instead of duplicating it. Package-private: nothing outside `sim`
 *  needs this directly, only the two elements whose I-V relation actually is a diode junction. */
final class DiodeMath {
	static final double THERMAL_VOLTAGE = 0.02585; // kT/q at ~300 K
	// Clamps the linearization point, not the actual solved voltage, so a large forward swing
	// between ticks can't send exp() to infinity.
	static final double MAX_LINEARIZATION_VOLTS = 0.85;

	record Companion(double geq, double ieq) {
	}

	private DiodeMath() {
	}

	static Companion linearize(double vPrev, double saturationCurrentAmps, double idealityFactor) {
		double vt = idealityFactor * THERMAL_VOLTAGE;
		double v0 = Math.min(vPrev, MAX_LINEARIZATION_VOLTS);
		double iAtV0 = saturationCurrentAmps * (Math.exp(v0 / vt) - 1);
		double geq = (saturationCurrentAmps / vt) * Math.exp(v0 / vt);
		double ieq = iAtV0 - geq * v0;
		return new Companion(geq, ieq);
	}
}
