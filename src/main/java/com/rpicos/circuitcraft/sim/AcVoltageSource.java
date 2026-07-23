package com.rpicos.circuitcraft.sim;

/** An ideal AC voltage source: either the one component under test during a Bode sweep (a
 *  non-zero complex amplitude, conventionally a real reference phase) or any other independent
 *  source in the same network forced to exactly zero volts, the usual small-signal convention
 *  ("voltage sources go to zero") that lets AC analysis isolate the response to the one signal
 *  actually being swept. */
public final class AcVoltageSource {
	private final int a, b;
	private final Complex value;

	int branchIndex = -1;
	private Complex current = Complex.ZERO;

	public AcVoltageSource(int a, int b, Complex value) {
		this.a = a;
		this.b = b;
		this.value = value;
	}

	public static AcVoltageSource zero(int a, int b) {
		return new AcVoltageSource(a, b, Complex.ZERO);
	}

	public int a() {
		return a;
	}

	public int b() {
		return b;
	}

	public Complex value() {
		return value;
	}

	void setSolvedCurrent(Complex current) {
		this.current = current;
	}

	/** The branch current solved for this source at the most recent {@link AcCircuit#solve}, in
	 *  the same a-to-b sense as {@link com.rpicos.circuitcraft.sim.VoltageSource#current()}. Zero
	 *  until the first solve. */
	public Complex current() {
		return current;
	}
}
