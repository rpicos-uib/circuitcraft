package com.rpicos.circuitcraft.sim;

public class Resistor implements Element {
	public final int a, b;
	public double ohms;

	public Resistor(int a, int b, double ohms) {
		this.a = a;
		this.b = b;
		this.ohms = ohms;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		circuit.stampConductance(mat, a, b, 1.0 / ohms);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		// stateless
	}

	public double current(Circuit circuit) {
		return (circuit.getVoltage(a) - circuit.getVoltage(b)) / ohms;
	}
}
