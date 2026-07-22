package com.rpicos.circuitcraft.sim;

/** A two-terminal passive device stamped into the circuit's MNA matrix each step. */
public interface Element {

	/** Add this element's contribution to the conductance matrix / RHS for the step ending at {@code newTime}. */
	void stamp(Circuit circuit, double[][] a, double[] z, double dt);

	/** Called after the linear solve, with node voltages already updated, to advance internal state (charge, flux, ...). */
	void updateState(Circuit circuit, double dt);
}
