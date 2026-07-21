package com.rpicos.minememristors.blockentity;

import java.util.ArrayList;
import java.util.List;

/** Fixed-size ring buffer of recent probe samples, shared by every {@link Probeable} implementation. */
final class ProbeHistory {
	private static final int SIZE = 200;

	private final float[] samples = new float[SIZE];
	private int writeIndex = 0;
	private int count = 0;

	void record(double value) {
		samples[writeIndex] = (float) value;
		writeIndex = (writeIndex + 1) % SIZE;
		count = Math.min(count + 1, SIZE);
	}

	/** Oldest-to-newest snapshot of recent samples. */
	List<Float> snapshot() {
		List<Float> out = new ArrayList<>(count);
		int start = (writeIndex - count + SIZE) % SIZE;
		for (int i = 0; i < count; i++) {
			out.add(samples[(start + i) % SIZE]);
		}
		return out;
	}
}
