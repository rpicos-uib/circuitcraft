package com.rpicos.circuitcraft.sim;

/** A voltage-controlled current source: injects current {@code gm * V[control]} into the
 *  output node, both referenced against the real 0V ground node (node 0) rather than a
 *  floating differential pair - see {@code GroundedComponentBlock}, which every block using
 *  this element requires to sit directly on a Ground block for exactly this reason. Since the
 *  relation is linear, {@link Circuit#stampTransconductance} alone captures it exactly and
 *  simultaneously with the rest of the tick's solve - no linearization-about-last-tick
 *  approximation needed here, unlike the nonlinear elements ({@link Diode}, {@link Bjt},
 *  {@link Mosfet}) that actually need one. */
public class Vccs implements Element {
	public final int nodeControl, nodeOutput;
	public double transconductanceSiemens;

	public Vccs(int nodeControl, int nodeOutput, double transconductanceSiemens) {
		this.nodeControl = nodeControl;
		this.nodeOutput = nodeOutput;
		this.transconductanceSiemens = transconductanceSiemens;
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		circuit.stampTransconductance(mat, nodeOutput, 0, nodeControl, 0, transconductanceSiemens);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
	}
}
