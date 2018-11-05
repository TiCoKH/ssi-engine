package common.scaler.xbrz;

import javax.annotation.Nonnull;

public class Scaler5x extends Scaler {
	private static final int SCALE = 5;

	public Scaler5x(@Nonnull ScalerConfig cfg) {
		super(cfg);
	}

	@Override
	public int scale() {
		return SCALE;
	}

	@Override
	public final void blendLineShallow(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(SCALE - 1, 0), col);
		alphaBlend(1, 4, out.ref(SCALE - 2, 2), col);
		alphaBlend(1, 4, out.ref(SCALE - 3, 4), col);
		alphaBlend(3, 4, out.ref(SCALE - 1, 1), col);
		alphaBlend(3, 4, out.ref(SCALE - 2, 3), col);
		out.ref(SCALE - 1, 2).set(col);
		out.ref(SCALE - 1, 3).set(col);
		out.ref(SCALE - 1, 4).set(col);
		out.ref(SCALE - 2, 4).set(col);
	}

	@Override
	public final void blendLineSteep(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(0, SCALE - 1), col);
		alphaBlend(1, 4, out.ref(2, SCALE - 2), col);
		alphaBlend(1, 4, out.ref(4, SCALE - 3), col);
		alphaBlend(3, 4, out.ref(1, SCALE - 1), col);
		alphaBlend(3, 4, out.ref(3, SCALE - 2), col);
		out.ref(2, SCALE - 1).set(col);
		out.ref(3, SCALE - 1).set(col);
		out.ref(4, SCALE - 1).set(col);
		out.ref(4, SCALE - 2).set(col);
	}

	@Override
	public final void blendLineSteepAndShallow(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(0, SCALE - 1), col);
		alphaBlend(1, 4, out.ref(2, SCALE - 2), col);
		alphaBlend(3, 4, out.ref(1, SCALE - 1), col);
		alphaBlend(1, 4, out.ref(SCALE - 1, 0), col);
		alphaBlend(1, 4, out.ref(SCALE - 2, 2), col);
		alphaBlend(3, 4, out.ref(SCALE - 1, 1), col);
		out.ref(2, SCALE - 1).set(col);
		out.ref(3, SCALE - 1).set(col);
		out.ref(SCALE - 1, 2).set(col);
		out.ref(SCALE - 1, 3).set(col);
		out.ref(4, SCALE - 1).set(col);
		alphaBlend(2, 3, out.ref(3, 3), col);
	}

	@Override
	public final void blendLineDiagonal(int col, OutputMatrix out) {
		alphaBlend(1, 8, out.ref(SCALE - 1, SCALE / 2), col);
		alphaBlend(1, 8, out.ref(SCALE - 2, SCALE / 2 + 1), col);
		alphaBlend(1, 8, out.ref(SCALE - 3, SCALE / 2 + 2), col);
		alphaBlend(7, 8, out.ref(4, 3), col);
		alphaBlend(7, 8, out.ref(3, 4), col);
		out.ref(4, 4).set(col);
	}

	@Override
	public final void blendCorner(int col, OutputMatrix out) {
		// model a round corner
		alphaBlend(86, 100, out.ref(4, 4), col); // exact: 0.8631434088
		alphaBlend(23, 100, out.ref(4, 3), col); // 0.2306749731
		alphaBlend(23, 100, out.ref(3, 4), col); // 0.2306749731
		// alphaBlend(8, 1000, out.ref(4, 2), col); //0.008384061834 -> negligable
		// alphaBlend(8, 1000, out.ref(2, 4), col); //0.008384061834
	}
}
