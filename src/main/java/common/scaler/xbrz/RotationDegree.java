package common.scaler.xbrz;

// clock-wise
enum RotationDegree {
	ROT_0, ROT_90, ROT_180, ROT_270;

	public final char rotate(final char b) {
		final int l = this.ordinal() << 1;
		final int r = 8 - l;

		return (char) (b << l | b >> r);
	}
}
