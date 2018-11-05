package common.scaler.xbrz;

// access matrix area, top-left at position "out" for image with given width
final class OutputMatrix {

	static final int maxRots = 4; // Number of 90 degree rotations
	static final int maxScale = 5; // Highest possible scale
	static final int maxScaleSq = maxScale * maxScale;
	static final IntPair[] matrixRotation;

	// calculate input matrix coordinates after rotation at program startup
	static {
		matrixRotation = new IntPair[(maxScale - 1) * maxScaleSq * maxRots];
		for (int n = 2; n < maxScale + 1; n++)
			for (int r = 0; r < maxRots; r++) {
				final int nr = (n - 2) * (maxRots * maxScaleSq) + r * maxScaleSq;
				for (int i = 0; i < maxScale; i++)
					for (int j = 0; j < maxScale; j++)
						matrixRotation[nr + i * maxScale + j] = buildMatrixRotation(r, i, j, n);
			}
	}

	private final IntPtr out;
	private int outi;
	private final int outWidth;
	private final int n;
	private int nr;

	public OutputMatrix(final int scale, final int[] out, final int outWidth) {
		this.n = (scale - 2) * (maxRots * maxScaleSq);
		this.out = new IntPtr(out);
		this.outWidth = outWidth;
	}

	public void move(final int rotDeg, final int outi) {
		this.nr = n + rotDeg * maxScaleSq;
		this.outi = outi;
	}

	public final IntPtr ref(final int i, final int j) {
		final IntPair rot = matrixRotation[nr + i * maxScale + j];
		out.position(outi + rot.J + rot.I * outWidth);
		return out;
	}

	private static final IntPair buildMatrixRotation(final int rotDeg, final int I, final int J, final int N) {
		final int I_old, J_old;

		if (rotDeg == 0) {
			I_old = I;
			J_old = J;
		} else {
			// old coordinates before rotation!
			final IntPair old = buildMatrixRotation(rotDeg - 1, I, J, N);
			I_old = N - 1 - old.J;
			J_old = old.I;
		}

		return new IntPair(I_old, J_old);
	}
}
