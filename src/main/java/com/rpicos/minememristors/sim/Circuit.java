package com.rpicos.minememristors.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A modified-nodal-analysis circuit: node 0 is always ground. Call {@link #addNode()} for every
 * other electrical node, add elements/sources referencing those node indices, then call
 * {@link #step(double)} once per simulation tick.
 */
public class Circuit {

	/** Tiny conductance to ground added to every node so floating/disconnected islands don't
	 *  produce a singular matrix (a component with an unwired terminal is a normal, not
	 *  exceptional, state in a world-built circuit). */
	private static final double GROUND_LEAK_SIEMENS = 1e-9;

	private int nodeCount = 1; // node 0 = ground
	private final List<Element> elements = new ArrayList<>();
	private final List<VoltageSource> sources = new ArrayList<>();
	private final List<IdealOpAmp> opAmps = new ArrayList<>();

	private double[] nodeVoltages = new double[1];
	private double time = 0;

	public int addNode() {
		return nodeCount++;
	}

	public void add(Element element) {
		elements.add(element);
	}

	public void add(VoltageSource source) {
		source.branchIndex = sources.size();
		sources.add(source);
	}

	public void add(IdealOpAmp opAmp) {
		opAmp.branchIndex = sources.size() + opAmps.size();
		opAmps.add(opAmp);
	}

	public double getVoltage(int node) {
		// Defensive bound: a node freshly added by rebuild() but not yet reached by a successful
		// step() (e.g. this tick's solve failed) must read as 0V rather than throw.
		return node == 0 || node >= nodeVoltages.length ? 0 : nodeVoltages[node];
	}

	public double time() {
		return time;
	}

	/** Adds conductance {@code g} between nodes a and b (ground-referenced nodes are skipped). */
	public void stampConductance(double[][] mat, int a, int b, double g) {
		if (a != 0) mat[a - 1][a - 1] += g;
		if (b != 0) mat[b - 1][b - 1] += g;
		if (a != 0 && b != 0) {
			mat[a - 1][b - 1] -= g;
			mat[b - 1][a - 1] -= g;
		}
	}

	/** Adds a Norton current source of value {@code i} flowing from node a to node b. */
	public void stampCurrentSource(double[] z, int a, int b, double i) {
		if (a != 0) z[a - 1] -= i;
		if (b != 0) z[b - 1] += i;
	}

	public void step(double dt) {
		int n = nodeCount - 1;
		int m = sources.size() + opAmps.size();
		int size = n + m;

		// Sized before solve() runs, so a component reading getVoltage() after a failed solve (an
		// exception thrown further down this method) still sees an in-bounds, if stale, array.
		if (nodeVoltages.length != nodeCount) {
			nodeVoltages = new double[nodeCount];
		}

		double[][] mat = new double[size][size];
		double[] z = new double[size];

		for (int i = 0; i < n; i++) {
			mat[i][i] += GROUND_LEAK_SIEMENS;
		}

		for (Element e : elements) {
			e.stamp(this, mat, z, dt);
		}

		double newTime = time + dt;
		for (VoltageSource s : sources) {
			int row = n + s.branchIndex;
			if (s.a != 0) {
				mat[row][s.a - 1] += 1;
				mat[s.a - 1][row] += 1;
			}
			if (s.b != 0) {
				mat[row][s.b - 1] -= 1;
				mat[s.b - 1][row] -= 1;
			}
			z[row] = s.waveform.valueAt(newTime);
		}

		// An ideal op-amp's branch-current unknown is injected only at its output node, but its
		// constraint equation (the "virtual short", v_plus == v_minus) references the two INPUT
		// nodes instead - unlike a VoltageSource, whose constraint and injection both reference
		// the same pair. That asymmetry is why this needs its own loop rather than reusing the
		// one above.
		for (IdealOpAmp op : opAmps) {
			int row = n + op.branchIndex;
			if (op.plus != 0) mat[row][op.plus - 1] += 1;
			if (op.minus != 0) mat[row][op.minus - 1] -= 1;
			z[row] = 0;
			if (op.out != 0) mat[op.out - 1][row] += 1;
		}

		double[] x = solve(mat, z);

		for (int i = 0; i < n; i++) {
			nodeVoltages[i + 1] = x[i];
		}
		for (VoltageSource s : sources) {
			s.setSolvedCurrent(x[n + s.branchIndex]);
		}
		for (IdealOpAmp op : opAmps) {
			op.setSolvedCurrent(x[n + op.branchIndex]);
		}

		for (Element e : elements) {
			e.updateState(this, dt);
		}

		time = newTime;
	}

	/** Gaussian elimination with partial pivoting. */
	private static double[] solve(double[][] a, double[] b) {
		int n = b.length;
		if (n == 0) return new double[0];

		for (int col = 0; col < n; col++) {
			int pivot = col;
			double best = Math.abs(a[col][col]);
			for (int row = col + 1; row < n; row++) {
				double v = Math.abs(a[row][col]);
				if (v > best) {
					best = v;
					pivot = row;
				}
			}
			if (best < 1e-15) {
				throw new ArithmeticException("Singular circuit matrix at column " + col
						+ " - check for unconnected node with no path to ground.");
			}
			if (pivot != col) {
				double[] tmpRow = a[col];
				a[col] = a[pivot];
				a[pivot] = tmpRow;
				double tmp = b[col];
				b[col] = b[pivot];
				b[pivot] = tmp;
			}

			for (int row = col + 1; row < n; row++) {
				double factor = a[row][col] / a[col][col];
				if (factor == 0) continue;
				for (int c = col; c < n; c++) {
					a[row][c] -= factor * a[col][c];
				}
				b[row] -= factor * b[col];
			}
		}

		double[] x = new double[n];
		for (int row = n - 1; row >= 0; row--) {
			double sum = b[row];
			for (int c = row + 1; c < n; c++) {
				sum -= a[row][c] * x[c];
			}
			x[row] = sum / a[row][row];
		}
		return x;
	}
}
