package com.rpicos.circuitcraft.sim;

/** A diode, linearized about the previous tick's terminal voltage into a companion
 *  conductance-plus-Norton-source pair - the same "reuse last tick's converged state" spirit
 *  the reactive elements' trapezoidal companion models already use, rather than iterating a
 *  full Newton-Raphson solve within a single tick. */
public class Diode implements Element, AcElement {
	private static final double THERMAL_VOLTAGE = DiodeMath.THERMAL_VOLTAGE;

	public final int a, b;
	public double saturationCurrentAmps;
	public double idealityFactor;

	private double vPrev = 0;

	public Diode(int a, int b, double saturationCurrentAmps, double idealityFactor) {
		this(a, b, saturationCurrentAmps, idealityFactor, 0);
	}

	/** As above, but seeding the linearization point directly rather than starting at 0V - used
	 *  to build a fresh AC-analysis instance (its own node indices, generally different from any
	 *  live transient instance's) that still linearizes about the same real operating point the
	 *  live diode has actually settled at, rather than restarting from an unbiased 0V guess. */
	public Diode(int a, int b, double saturationCurrentAmps, double idealityFactor, double initialVoltage) {
		this.a = a;
		this.b = b;
		this.saturationCurrentAmps = saturationCurrentAmps;
		this.idealityFactor = idealityFactor;
		this.vPrev = initialVoltage;
	}

	/** The terminal voltage this diode is currently linearized about (last tick's solved value). */
	public double lastVoltage() {
		return vPrev;
	}

	/** Shockley diode equation, evaluated exactly (unclamped) at an arbitrary voltage - used for
	 *  the probe readout, as opposed to the clamped linearization point used for stamping. */
	public double currentAt(double v) {
		double vt = idealityFactor * THERMAL_VOLTAGE;
		return saturationCurrentAmps * (Math.exp(v / vt) - 1);
	}

	/** Small-signal conductance {@code dI/dV} at the diode's last known DC operating point -
	 *  used by the AC small-signal stamp below. */
	private double smallSignalConductance() {
		return DiodeMath.linearize(vPrev, saturationCurrentAmps, idealityFactor).geq();
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		DiodeMath.Companion companion = DiodeMath.linearize(vPrev, saturationCurrentAmps, idealityFactor);
		circuit.stampConductance(mat, a, b, companion.geq());
		circuit.stampCurrentSource(z, a, b, companion.ieq());
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		vPrev = circuit.getVoltage(a) - circuit.getVoltage(b);
	}

	/** AC case: a diode has no single fixed impedance - it's linearized about its DC operating
	 *  point into a small-signal resistance {@code r_d = 1/(dI/dV)}, the classic
	 *  {@code r_d = V_T/I_D} diode small-signal model, reusing exactly the same operating point
	 *  and conductance the transient companion stamp above already computes each tick. */
	@Override
	public void stampAc(AcCircuit circuit, Complex[][] mat, Complex[] z, double omega) {
		circuit.stampAdmittance(mat, a, b, Complex.real(smallSignalConductance()));
	}
}
