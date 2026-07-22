package com.rpicos.circuitcraft.sim;

/** Trapezoidal-integration companion model: a conductance in parallel with a history current source. */
public class Capacitor implements Element {
	public final int a, b;
	public double farads;

	private double vPrev = 0;
	private double iPrev = 0;

	public Capacitor(int a, int b, double farads) {
		this.a = a;
		this.b = b;
		this.farads = farads;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		double geq = 2 * farads / dt;
		double ieq = -(geq * vPrev + iPrev);
		circuit.stampConductance(mat, a, b, geq);
		circuit.stampCurrentSource(z, a, b, ieq);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		double geq = 2 * farads / dt;
		double vNow = circuit.getVoltage(a) - circuit.getVoltage(b);
		double iNow = geq * (vNow - vPrev) - iPrev;
		vPrev = vNow;
		iPrev = iNow;
	}

	public double current() {
		return iPrev;
	}

	public double voltage() {
		return vPrev;
	}
}
