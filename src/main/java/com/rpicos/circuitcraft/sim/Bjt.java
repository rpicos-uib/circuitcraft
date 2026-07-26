package com.rpicos.circuitcraft.sim;

/** Simplest large-signal BJT model: the base-emitter junction is an ordinary diode (reusing
 *  {@link DiodeMath}'s exact companion linearization), and the collector current is a fixed
 *  multiple ({@code beta}) of whatever base current that junction is conducting - the classic
 *  intro-course "current-gain" picture, deliberately not a full Ebers-Moll model (no separate
 *  saturation/cutoff regions beyond the base-emitter junction itself simply not conducting
 *  below its turn-on voltage, no early effect, no base-width modulation). {@code beta} is the
 *  one editable parameter, matching the "simplest model, one knob" brief this component family
 *  was built to.
 *
 *  <p>{@code polarity} is {@code +1} for an NPN (base-emitter junction conducts base-to-emitter
 *  like a normal diode when forward biased; collector current flows collector-to-emitter) or
 *  {@code -1} for a PNP (junction conducts emitter-to-base instead; collector current flows
 *  emitter-to-collector) - the two device types are the exact same equations mirrored, the same
 *  pattern {@link Mosfet} uses for NMOS/PMOS. */
public class Bjt implements Element {
	public final int base, collector, emitter;
	public double beta;
	public double saturationCurrentAmps;
	public double idealityFactor;
	public final int polarity;

	private double vbePrev = 0;

	public Bjt(int base, int collector, int emitter, double beta, double saturationCurrentAmps,
			double idealityFactor, int polarity) {
		this.base = base;
		this.collector = collector;
		this.emitter = emitter;
		this.beta = beta;
		this.saturationCurrentAmps = saturationCurrentAmps;
		this.idealityFactor = idealityFactor;
		this.polarity = polarity;
	}

	/** Base current at an arbitrary Vbe (signed the NPN way; PNP's own effective junction
	 *  voltage is {@code polarity * vbe}) - used for the probe readout. */
	public double baseCurrentAt(double vbe) {
		double vt = idealityFactor * DiodeMath.THERMAL_VOLTAGE;
		return saturationCurrentAmps * (Math.exp(polarity * vbe / vt) - 1);
	}

	@Override
	public void stamp(Circuit circuit, double[][] mat, double[] z, double dt) {
		double vJunction = polarity * vbePrev;
		DiodeMath.Companion be = DiodeMath.linearize(vJunction, saturationCurrentAmps, idealityFactor);

		// Base-emitter junction: an ordinary diode, forward-conducting node first (base for
		// NPN, emitter for PNP) to the other.
		int anode = polarity > 0 ? base : emitter;
		int cathode = polarity > 0 ? emitter : base;
		circuit.stampConductance(mat, anode, cathode, be.geq());
		circuit.stampCurrentSource(z, anode, cathode, be.ieq());

		// Collector current = beta * base current, flowing collector-to-emitter (NPN) or
		// emitter-to-collector (PNP), controlled by the same (base, emitter) voltage pair.
		int high = polarity > 0 ? collector : emitter;
		int low = polarity > 0 ? emitter : collector;
		double gmSigned = beta * be.geq() * polarity;
		double ieqCollector = beta * be.ieq();
		circuit.stampTransconductance(mat, high, low, base, emitter, gmSigned);
		circuit.stampCurrentSource(z, high, low, ieqCollector);
	}

	@Override
	public void updateState(Circuit circuit, double dt) {
		vbePrev = circuit.getVoltage(base) - circuit.getVoltage(emitter);
	}
}
