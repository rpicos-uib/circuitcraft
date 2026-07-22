package com.rpicos.circuitcraft.sim;

/** A time-varying source value, in volts, as a function of simulation time in seconds. */
public interface Waveform {

	double valueAt(double timeSeconds);

	static Waveform dc(double volts) {
		return t -> volts;
	}

	static Waveform sine(double amplitude, double frequencyHz, double phaseRad, double offset) {
		return t -> offset + amplitude * Math.sin(2 * Math.PI * frequencyHz * t + phaseRad);
	}

	static Waveform square(double amplitude, double frequencyHz, double offset) {
		return t -> {
			double phase = (t * frequencyHz) % 1.0;
			return offset + (phase < 0.5 ? amplitude : -amplitude);
		};
	}

	static Waveform triangle(double amplitude, double frequencyHz, double offset) {
		return t -> {
			double phase = (t * frequencyHz) % 1.0;
			double tri = phase < 0.5
					? (4 * phase - 1)
					: (3 - 4 * phase);
			return offset + amplitude * tri;
		};
	}
}
