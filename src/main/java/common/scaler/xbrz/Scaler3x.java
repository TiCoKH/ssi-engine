package common.scaler.xbrz;

import javax.annotation.Nonnull;

public class Scaler3x extends Scaler {
	private static final int SCALE = 3;

	public Scaler3x(@Nonnull ScalerConfig cfg) {
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
		out.ref(SCALE - 1, 2).set(col);
	}

	@Override
	public final void blendLineSteep(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(0, SCALE - 1), col);
		alphaBlend(1, 4, out.ref(2, SCALE - 2), col);
		alphaBlend(3, 4, out.ref(1, SCALE - 1), col);
		out.ref(2, SCALE - 1).set(col);
	}

	@Override
	public final void blendLineSteepAndShallow(int col, OutputMatrix out) {
		alphaBlend(1, 4, out.ref(2, 0), col);
		alphaBlend(1, 4, out.ref(0, 2), col);
		alphaBlend(3, 4, out.ref(2, 1), col);
		alphaBlend(3, 4, out.ref(1, 2), col);

		out.ref(2, 2).set(col);
	}

	@Override
	public final void blendLineDiagonal(int col, OutputMatrix out) {
		alphaBlend(1, 8, out.ref(1, 2), col);
		alphaBlend(1, 8, out.ref(2, 1), col);
		alphaBlend(7, 8, out.ref(2, 2), col);
	}

	@Override
	public final void blendCorner(int col, OutputMatrix out) {
		// model a round corner
		alphaBlend(45, 100, out.ref(2, 2), col); // exact: 0.4545939598
		// alphaBlend(14, 1000, out.ref(2, 1), col); //0.01413008627 -> negligable
		// alphaBlend(14, 1000, out.ref(1, 2), col); //0.01413008627
	}
}
