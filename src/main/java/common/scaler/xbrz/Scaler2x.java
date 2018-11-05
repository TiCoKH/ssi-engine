package common.scaler.xbrz;

import javax.annotation.Nonnull;

public class Scaler2x extends Scaler {
	private static final int SCALE = 2;

	public Scaler2x(@Nonnull ScalerConfig cfg) {
		super(cfg);
	}

	@Override
	public int scale() {
		return SCALE;
	}

	@Override
	public final void blendLineShallow(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(SCALE - 1, 0), col);
		alphaBlend(3, 4, out.ref(SCALE - 1, 1), col);
	}

	@Override
	public final void blendLineSteep(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(0, SCALE - 1), col);
		alphaBlend(3, 4, out.ref(1, SCALE - 1), col);
	}

	@Override
	public final void blendLineSteepAndShallow(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(1, 0), col);
		alphaBlend(1, 4, out.ref(0, 1), col);
		alphaBlend(5, 6, out.ref(1, 1), col); // fixes 7/8 used in xBR
	}

	@Override
	public final void blendLineDiagonal(int col, OutputMatrix out) {
		alphaBlend(1, 2, out.ref(1, 1), col);
	}

	@Override
	public final void blendCorner(int col, OutputMatrix out) {
		// model a round corner
		alphaBlend(21, 100, out.ref(1, 1), col); // exact: 1 - pi/4 = 0.2146018366
	}
}
