package com.rpicos.minememristors.sim;

/** An ideal operational amplifier: infinite open-loop gain, infinite input impedance (no
 *  current drawn by {@code plus}/{@code minus}), zero output impedance. Modeled the standard
 *  "nullor" way in MNA - not as a resistive element, but as its own branch-current unknown,
 *  the same mechanism {@link VoltageSource} uses, except the constraint equation
 *  ({@code v_plus == v_minus}, the "virtual short" every negative-feedback op-amp circuit
 *  relies on) references different nodes than the ones the branch current is injected into
 *  ({@code out}). That asymmetry is why this isn't just a VoltageSource with value 0. */
public class IdealOpAmp {
	public final int plus, minus, out;

	int branchIndex = -1;
	private double outputCurrent;

	public IdealOpAmp(int plus, int minus, int out) {
		this.plus = plus;
		this.minus = minus;
		this.out = out;
	}

	void setSolvedCurrent(double current) {
		this.outputCurrent = current;
	}

	/** Current the op-amp's output is sourcing into the rest of the circuit. */
	public double outputCurrent() {
		return outputCurrent;
	}
}
