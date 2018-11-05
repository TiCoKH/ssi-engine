package common.scaler.xbrz;

import javax.annotation.Nonnull;

public class Scaler4x extends Scaler {
	private static final int SCALE = 4;

	public Scaler4x(@Nonnull ScalerConfig cfg) {
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
		alphaBlend(3, 4, out.ref(SCALE - 1, 1), col);
		alphaBlend(3, 4, out.ref(SCALE - 2, 3), col);
		out.ref(SCALE - 1, 2).set(col);
		out.ref(SCALE - 1, 3).set(col);
	}

	@Override
	public final void blendLineSteep(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(0, SCALE - 1), col);
		alphaBlend(1, 4, out.ref(2, SCALE - 2), col);
		alphaBlend(3, 4, out.ref(1, SCALE - 1), col);
		alphaBlend(3, 4, out.ref(3, SCALE - 2), col);
		out.ref(2, SCALE - 1).set(col);
		out.ref(3, SCALE - 1).set(col);
	}

	@Override
	public final void blendLineSteepAndShallow(int col, OutputMatrix out) {
		alphaBlend(3, 4, out.ref(3, 1), col);
		alphaBlend(3, 4, out.ref(1, 3), col);
		alphaBlend(1, 4, out.ref(3, 0), col);
		alphaBlend(1, 4, out.ref(0, 3), col);
		alphaBlend(1, 3, out.ref(2, 2), col); // fixes 1/4 used in xBR
		out.ref(3, 3).set(col);
		out.ref(3, 2).set(col);
		out.ref(2, 3).set(col);
	}

	@Override
	public final void blendLineDiagonal(int col, OutputMatrix out) {
		alphaBlend(1, 2, out.ref(SCALE - 1, SCALE / 2), col);
		alphaBlend(1, 2, out.ref(SCALE - 2, SCALE / 2 + 1), col);
		out.ref(SCALE - 1, SCALE - 1).set(col);
	}

	@Override
	public final void blendCorner(int col, OutputMatrix out) {
		// model a round corner
		alphaBlend(68, 100, out.ref(3, 3), col); // exact: 0.6848532563
		alphaBlend(9, 100, out.ref(3, 2), col); // 0.08677704501
		alphaBlend(9, 100, out.ref(2, 3), col); // 0.08677704501
	}
}
