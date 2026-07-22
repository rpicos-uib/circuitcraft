package com.rpicos.circuitcraft.sim;

/** An ideal independent voltage source. Gets its own branch-current unknown in the MNA solve. */
public class VoltageSource {
	public final int a, b;
	public Waveform waveform;

	int branchIndex = -1;
	private double current;

	public VoltageSource(int a, int b, Waveform waveform) {
		this.a = a;
		this.b = b;
		this.waveform = waveform;
	}

	void setSolvedCurrent(double current) {
		this.current = current;
	}

	/** Current flowing from node a to node b through the source. */
	public double current() {
		return current;
	}
}
