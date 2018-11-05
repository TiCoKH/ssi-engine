package common.scaler.xbrz;

import javax.annotation.Nonnull;

final class ColorComparator {
	private static final int RED_MASK = 0xff0000;
	private static final int GREEN_MASK = 0x00ff00;
	private static final int BLUE_MASK = 0x0000ff;

	private ScalerConfig cfg;
	private double colorThreshold;

	public ColorComparator(@Nonnull ScalerConfig cfg) {
		this.cfg = cfg;
		this.colorThreshold = square(cfg.equalColorTolerance);
	}

	public double distance(int color1, int color2) {
		return colorDist(color1, color2, cfg.luminanceWeight);
	}

	public boolean compare(int color1, int color2) {
		return colorDist(color1, color2, cfg.luminanceWeight) < colorThreshold;
	}

	private static final double colorDist(final int pix1, final int pix2, final double luminanceWeight) {
		if (pix1 == pix2)
			return 0;

		return distYCbCr(pix1, pix2, luminanceWeight);
	}

	private static final double distYCbCr(final int pix1, final int pix2, final double lumaWeight) {
		// http://en.wikipedia.org/wiki/YCbCr#ITU-R_BT.601_conversion
		// YCbCr conversion is a matrix multiplication => take advantage of linearity by subtracting first!
		// we may delay division by 255 to after matrix multiplication
		final int r_diff = ((pix1 & RED_MASK) - (pix2 & RED_MASK)) >> 16;
		final int g_diff = ((pix1 & GREEN_MASK) - (pix2 & GREEN_MASK)) >> 8;
		// subtraction for int is noticeable faster than for double
		final int b_diff = (pix1 & BLUE_MASK) - (pix2 & BLUE_MASK);

		// ITU-R BT.709 conversion
		final double k_b = 0.0722;
		final double k_r = 0.2126;
		final double k_g = 1 - k_b - k_r;

		final double scale_b = 0.5 / (1 - k_b);
		final double scale_r = 0.5 / (1 - k_r);

		// [!], analog YCbCr!
		final double y = k_r * r_diff + k_g * g_diff + k_b * b_diff;
		final double c_b = scale_b * (b_diff - y);
		final double c_r = scale_r * (r_diff - y);

		// Skip division by 255.
		// Also skip square root here by pre-squaring the
		// config option equalColorTolerance.
		// return Math.sqrt(square(lumaWeight * y) + square(c_b) + square(c_r));
		return square(lumaWeight * y) + square(c_b) + square(c_r);
	}

	private static final double square(final double value) {
		return value * value;
	}
}
