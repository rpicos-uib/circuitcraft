package com.rpicos.circuitcraft.sim;

import java.util.ArrayList;
import java.util.List;

/**
 * A single-frequency small-signal (AC) modified-nodal-analysis solve: the complex-valued
 * counterpart of {@link Circuit}, built fresh for one angular frequency at a time rather than
 * stepped forward in time. Node 0 is always ground, exactly as in the transient solver.
 *
 * <p>Every element stamped here contributes a frequency-dependent complex admittance rather
 * than a real conductance (a capacitor's admittance is {@code j*omega*C}, an inductor's is
 * {@code 1/(j*omega*L)}, and so on - see each element's own {@code stampAc} method). Independent
 * voltage sources other than the one under test are, per the usual small-signal convention,
 * stamped at exactly zero volts (a short) rather than omitted, since AC analysis characterizes
 * the circuit's response to a perturbation on top of whatever operating point those sources
 * would otherwise establish.
 */
public class AcCircuit {
	private static final double GROUND_LEAK_SIEMENS = 1e-9;

	private int nodeCount = 1; // node 0 = ground
	private final List<AcElement> elements = new ArrayList<>();
	private final List<AcVoltageSource> sources = new ArrayList<>();
	private final List<AcOpAmp> opAmps = new ArrayList<>();

	private Complex[] nodeVoltages = new Complex[1];

	public int addNode() {
		return nodeCount++;
	}

	public void add(AcElement element) {
		elements.add(element);
	}

	public void add(AcVoltageSource source) {
		sources.add(source);
	}

	public void add(AcOpAmp opAmp) {
		opAmps.add(opAmp);
	}

	/** See {@link Circuit#assignBranchIndices} - the exact same fix, needed for the exact same
	 *  reason: add() can't know the final branch numbering when op-amps and voltage sources are
	 *  added in an unpredictable interleaved order. */
	private void assignBranchIndices() {
		for (int i = 0; i < sources.size(); i++) {
			sources.get(i).branchIndex = i;
		}
		for (int i = 0; i < opAmps.size(); i++) {
			opAmps.get(i).branchIndex = sources.size() + i;
		}
	}

	public Complex getVoltage(int node) {
		return node == 0 || node >= nodeVoltages.length ? Complex.ZERO : nodeVoltages[node];
	}

	public void stampAdmittance(Complex[][] mat, int a, int b, Complex y) {
		if (a != 0) mat[a - 1][a - 1] = mat[a - 1][a - 1].add(y);
		if (b != 0) mat[b - 1][b - 1] = mat[b - 1][b - 1].add(y);
		if (a != 0 && b != 0) {
			mat[a - 1][b - 1] = mat[a - 1][b - 1].subtract(y);
			mat[b - 1][a - 1] = mat[b - 1][a - 1].subtract(y);
		}
	}

	public void stampCurrentSource(Complex[] z, int a, int b, Complex i) {
		if (a != 0) z[a - 1] = z[a - 1].subtract(i);
		if (b != 0) z[b - 1] = z[b - 1].add(i);
	}

	/** Solves the network once at the given angular frequency (rad/s); {@link #getVoltage} then
	 *  reflects that frequency's result until the next call. */
	public void solve(double omega) {
		assignBranchIndices();

		int n = nodeCount - 1;
		int m = sources.size() + opAmps.size();
		int size = n + m;

		nodeVoltages = new Complex[nodeCount];

		Complex[][] mat = new Complex[size][size];
		Complex[] z = new Complex[size];
		for (int i = 0; i < size; i++) {
			z[i] = Complex.ZERO;
			for (int j = 0; j < size; j++) {
				mat[i][j] = Complex.ZERO;
			}
		}
		for (int i = 0; i < n; i++) {
			mat[i][i] = mat[i][i].add(Complex.real(GROUND_LEAK_SIEMENS));
		}

		for (AcElement e : elements) {
			e.stampAc(this, mat, z, omega);
		}

		for (AcVoltageSource s : sources) {
			int row = n + s.branchIndex;
			if (s.a() != 0) {
				mat[row][s.a() - 1] = mat[row][s.a() - 1].add(Complex.ONE);
				mat[s.a() - 1][row] = mat[s.a() - 1][row].add(Complex.ONE);
			}
			if (s.b() != 0) {
				mat[row][s.b() - 1] = mat[row][s.b() - 1].subtract(Complex.ONE);
				mat[s.b() - 1][row] = mat[s.b() - 1][row].subtract(Complex.ONE);
			}
			z[row] = s.value();
		}

		// Same asymmetric constraint-vs-injection pattern as IdealOpAmp in the transient solver,
		// just with a frequency-dependent complex gain instead of an ideal infinite one: the
		// constraint row reads the two input nodes, but the branch current it introduces is
		// injected only at the output node.
		for (AcOpAmp op : opAmps) {
			int row = n + op.branchIndex;
			Complex gain = op.gainAt(omega);
			if (op.out() != 0) {
				mat[row][op.out() - 1] = mat[row][op.out() - 1].add(Complex.ONE);
				mat[op.out() - 1][row] = mat[op.out() - 1][row].add(Complex.ONE);
			}
			if (op.plus() != 0) mat[row][op.plus() - 1] = mat[row][op.plus() - 1].subtract(gain);
			if (op.minus() != 0) mat[row][op.minus() - 1] = mat[row][op.minus() - 1].add(gain);
			z[row] = Complex.ZERO;
		}

		Complex[] x = solve(mat, z);
		for (int i = 0; i < n; i++) {
			nodeVoltages[i + 1] = x[i];
		}
		for (AcVoltageSource s : sources) {
			s.setSolvedCurrent(x[n + s.branchIndex]);
		}
	}

	/** Complex Gaussian elimination with partial pivoting (by magnitude). */
	private static Complex[] solve(Complex[][] a, Complex[] b) {
		int n = b.length;
		if (n == 0) return new Complex[0];

		for (int col = 0; col < n; col++) {
			int pivot = col;
			double best = a[col][col].magnitude();
			for (int row = col + 1; row < n; row++) {
				double v = a[row][col].magnitude();
				if (v > best) {
					best = v;
					pivot = row;
				}
			}
			if (best < 1e-15) {
				throw new ArithmeticException("Singular AC circuit matrix at column " + col
						+ " - check for unconnected node with no path to ground.");
			}
			if (pivot != col) {
				Complex[] tmpRow = a[col];
				a[col] = a[pivot];
				a[pivot] = tmpRow;
				Complex tmp = b[col];
				b[col] = b[pivot];
				b[pivot] = tmp;
			}

			for (int row = col + 1; row < n; row++) {
				Complex factor = a[row][col].divide(a[col][col]);
				if (factor.magnitude() == 0) continue;
				for (int c = col; c < n; c++) {
					a[row][c] = a[row][c].subtract(factor.multiply(a[col][c]));
				}
				b[row] = b[row].subtract(factor.multiply(b[col]));
			}
		}

		Complex[] x = new Complex[n];
		for (int row = n - 1; row >= 0; row--) {
			Complex sum = b[row];
			for (int c = row + 1; c < n; c++) {
				sum = sum.subtract(a[row][c].multiply(x[c]));
			}
			x[row] = sum.divide(a[row][row]);
		}
		return x;
	}
}
