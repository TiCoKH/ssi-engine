package common.scaler.xbrz;

import javax.annotation.Nonnull;

/*
 * -------------------------------------------------------------------------
 * | xBRZ: "Scale by rules" - high quality image upscaling filter by Zenju |
 * -------------------------------------------------------------------------
 * Java port originally from
 * http://intrepidis.blogspot.com/2014/02/xbrz-in-java.html
 * 
 * using a modified approach of xBR:
 * http://board.byuu.org/viewtopic.php?f=10&t=2248
 * - new rule set preserving small image features
 * - support multithreading
 * - support 64 bit architectures
 * - support processing image slices
 * 
 * -> map source (srcWidth * srcHeight) to target (scale * width x scale * height)
 * image, optionally processing a half-open slice of rows [yFirst, yLast) only
 * -> color format: ARGB (BGRA char order), alpha channel unused
 * -> support for source/target pitch in chars!
 * -> if your emulator changes only a few image slices during each cycle
 * (e.g. Dosbox) then there's no need to run xBRZ on the complete image:
 * Just make sure you enlarge the source image slice by 2 rows on top and
 * 2 on bottom (this is the additional range the xBRZ algorithm is using
 * during analysis)
 * Caveat: If there are multiple changed slices, make sure they do not overlap
 * after adding these additional rows in order to avoid a memory race condition
 * if you are using multiple threads for processing each enlarged slice!
 * 
 * THREAD-SAFETY: - parts of the same image may be scaled by multiple threads
 * as long as the [yFirst, yLast) ranges do not overlap!
 * - there is a minor inefficiency for the first row of a slice, so avoid
 * processing single rows only
 * 
 * Converted to Java 7 by Intrepidis. It would have been nice to use
 * Java 8 lambdas, but Java 7 is more ubiquitous at the time of writing,
 * so this code uses anonymous local classes instead.
 * Regarding multithreading, each thread should have its own instance
 * of the xBRZ class.
 * 
 * To make use of the scaleImage() method you might need to use these functions:
 * 
 * public static final int[] fromBitmap(final Bitmap source) {
 * final int width = source.getWidth();
 * final int height = source.getHeight();
 * final int[] target = new int[width * height];
 * source.getPixels(target, 0, width, 0, 0, width, height);
 * return target;
 * }
 * 
 * public static final Bitmap toBitmap(final int[] source, final int sourceWidth, final int sourceHeight) {
 * final Bitmap target = Bitmap.createBitmap(source, sourceWidth, sourceHeight, Bitmap.Config.ARGB_8888);
 * return target;
 * }
 */
public abstract class Scaler {
	// These blend types must fit into 2 bits.
	private static final char BLEND_NONE = 0; // do not blend
	private static final char BLEND_NORMAL = 1; // a normal indication to blend
	private static final char BLEND_DOMINANT = 2; // a strong indication to blend

	private static final int redMask = 0xff0000;
	private static final int greenMask = 0x00ff00;
	private static final int blueMask = 0x0000ff;

	private ScalerConfig cfg;
	private ColorComparator cmp;

	public Scaler(@Nonnull ScalerConfig cfg) {
		this.cfg = cfg;
		this.cmp = new ColorComparator(cfg);
	}

	public final void scaleImage(@Nonnull int[] src, @Nonnull int[] target, int srcWidth, int srcHeight, int yFirst, int yLast) {
		yFirst = Math.max(yFirst, 0);
		yLast = Math.min(yLast, srcHeight);

		if (yFirst >= yLast || srcWidth <= 0)
			return;

		final int targetWidth = srcWidth * scale();

		// temporary buffer for "on the fly preprocessing"
		final char[] preProcBuffer = new char[srcWidth];

		final Kernel4x4 ker4 = new Kernel4x4();
		final BlendResult blendResult = new BlendResult();

		// initialize preprocessing buffer for first row:
		// detect upper left and right corner blending
		// this cannot be optimized for adjacent processing
		// stripes; we must not allow for a memory race condition!
		if (yFirst > 0) {
			final int y = yFirst - 1;

			final int s_m1 = srcWidth * Math.max(y - 1, 0);
			final int s_0 = srcWidth * y; // center line
			final int s_p1 = srcWidth * Math.min(y + 1, srcHeight - 1);
			final int s_p2 = srcWidth * Math.min(y + 2, srcHeight - 1);

			for (int x = 0; x < srcWidth; ++x) {
				final int x_m1 = Math.max(x - 1, 0);
				final int x_p1 = Math.min(x + 1, srcWidth - 1);
				final int x_p2 = Math.min(x + 2, srcWidth - 1);

				// read sequentially from memory as far as possible
				ker4.a = src[s_m1 + x_m1];
				ker4.b = src[s_m1 + x];
				ker4.c = src[s_m1 + x_p1];
				ker4.d = src[s_m1 + x_p2];

				ker4.e = src[s_0 + x_m1];
				ker4.f = src[s_0 + x];
				ker4.g = src[s_0 + x_p1];
				ker4.h = src[s_0 + x_p2];

				ker4.i = src[s_p1 + x_m1];
				ker4.j = src[s_p1 + x];
				ker4.k = src[s_p1 + x_p1];
				ker4.l = src[s_p1 + x_p2];

				ker4.m = src[s_p2 + x_m1];
				ker4.n = src[s_p2 + x];
				ker4.o = src[s_p2 + x_p1];
				ker4.p = src[s_p2 + x_p2];

				preProcessCorners(ker4, blendResult); // writes to blendResult
				/*
				 * preprocessing blend result:
				 * ---------
				 * | F | G | //evalute corner between F, G, J, K
				 * ----|---| //input pixel is at position F
				 * | J | K |
				 * ---------
				 */
				preProcBuffer[x] = BlendInfo.setTopR(preProcBuffer[x], blendResult.j);

				if (x + 1 < srcWidth)
					preProcBuffer[x + 1] = BlendInfo.setTopL(preProcBuffer[x + 1], blendResult.k);
			}
		}

		OutputMatrix outputMatrix = new OutputMatrix(scale(), target, targetWidth);

		char blend_xy = 0;
		char blend_xy1 = 0;

		final Kernel3x3 ker3 = new Kernel3x3();

		for (int y = yFirst; y < yLast; ++y) {
			// consider MT "striped" access
			int trgi = scale() * y * targetWidth;

			final int s_m1 = srcWidth * Math.max(y - 1, 0);
			final int s_0 = srcWidth * y; // center line
			final int s_p1 = srcWidth * Math.min(y + 1, srcHeight - 1);
			final int s_p2 = srcWidth * Math.min(y + 2, srcHeight - 1);

			blend_xy1 = 0; // corner blending for current (x, y + 1) position

			for (int x = 0; x < srcWidth; ++x, trgi += scale()) {
				final int x_m1 = Math.max(x - 1, 0);
				final int x_p1 = Math.min(x + 1, srcWidth - 1);
				final int x_p2 = Math.min(x + 2, srcWidth - 1);

				// evaluate the four corners on bottom-right of current pixel
				// blend_xy for current (x, y) position
				{
					// read sequentially from memory as far as possible
					ker4.a = src[s_m1 + x_m1];
					ker4.b = src[s_m1 + x];
					ker4.c = src[s_m1 + x_p1];
					ker4.d = src[s_m1 + x_p2];

					ker4.e = src[s_0 + x_m1];
					ker4.f = src[s_0 + x];
					ker4.g = src[s_0 + x_p1];
					ker4.h = src[s_0 + x_p2];

					ker4.i = src[s_p1 + x_m1];
					ker4.j = src[s_p1 + x];
					ker4.k = src[s_p1 + x_p1];
					ker4.l = src[s_p1 + x_p2];

					ker4.m = src[s_p2 + x_m1];
					ker4.n = src[s_p2 + x];
					ker4.o = src[s_p2 + x_p1];
					ker4.p = src[s_p2 + x_p2];

					preProcessCorners(ker4, blendResult); // writes to blendResult

					/*
					 * preprocessing blend result:
					 * ---------
					 * | F | G | // evaluate corner between F, G, J, K
					 * ----|---| // current input pixel is at position F
					 * | J | K |
					 * ---------
					 */

					// all four corners of (x, y) have been determined at
					// this point due to processing sequence!
					blend_xy = BlendInfo.setBottomR(preProcBuffer[x], blendResult.f);

					// set 2nd known corner for (x, y + 1)
					blend_xy1 = BlendInfo.setTopR(blend_xy1, blendResult.j);
					// store on current buffer position for use on next row
					preProcBuffer[x] = blend_xy1;

					// set 1st known corner for (x + 1, y + 1) and
					// buffer for use on next column
					blend_xy1 = BlendInfo.setTopL((char) 0, blendResult.k);

					if (x + 1 < srcWidth)
						// set 3rd known corner for (x + 1, y)
						preProcBuffer[x + 1] = BlendInfo.setBottomL(preProcBuffer[x + 1], blendResult.g);
				}

				// fill block of size scale * scale with the given color
				// place *after* preprocessing step, to not overwrite the
				// results while processing the the last pixel!
				fillBlock(target, trgi, targetWidth, src[s_0 + x], scale());

				// blend four corners of current pixel
				if (blend_xy == 0)
					continue;

				final int a = 0, b = 1, c = 2, d = 3, e = 4, f = 5, g = 6, h = 7, i = 8;

				// read sequentially from memory as far as possible
				ker3.data[a] = src[s_m1 + x_m1];
				ker3.data[b] = src[s_m1 + x];
				ker3.data[c] = src[s_m1 + x_p1];

				ker3.data[d] = src[s_0 + x_m1];
				ker3.data[e] = src[s_0 + x];
				ker3.data[f] = src[s_0 + x_p1];

				ker3.data[g] = src[s_p1 + x_m1];
				ker3.data[h] = src[s_p1 + x];
				ker3.data[i] = src[s_p1 + x_p1];

				scalePixel(RotationDegree.ROT_0.ordinal(), ker3, outputMatrix, target, trgi, targetWidth, blend_xy);
				scalePixel(RotationDegree.ROT_90.ordinal(), ker3, outputMatrix, target, trgi, targetWidth, blend_xy);
				scalePixel(RotationDegree.ROT_180.ordinal(), ker3, outputMatrix, target, trgi, targetWidth, blend_xy);
				scalePixel(RotationDegree.ROT_270.ordinal(), ker3, outputMatrix, target, trgi, targetWidth, blend_xy);
			}
		}
	}

	// detect blend direction
	private void preProcessCorners(final Kernel4x4 ker, final BlendResult blendResult) {
		blendResult.reset();

		if ((ker.f == ker.g && ker.j == ker.k) || (ker.f == ker.j && ker.g == ker.k))
			return;

		final int weight = 4;
		final double jg = cmp.distance(ker.i, ker.f) + cmp.distance(ker.f, ker.c) //
			+ cmp.distance(ker.n, ker.k) + cmp.distance(ker.k, ker.h) //
			+ weight * cmp.distance(ker.j, ker.g);
		final double fk = cmp.distance(ker.e, ker.j) + cmp.distance(ker.j, ker.o) //
			+ cmp.distance(ker.b, ker.g) + cmp.distance(ker.g, ker.l) //
			+ weight * cmp.distance(ker.f, ker.k);

		if (jg < fk) {
			final boolean dominantGradient = cfg.dominantDirectionThreshold * jg < fk;
			if (ker.f != ker.g && ker.f != ker.j)
				blendResult.f = dominantGradient ? BLEND_DOMINANT : BLEND_NORMAL;

			if (ker.k != ker.j && ker.k != ker.g)
				blendResult.k = dominantGradient ? BLEND_DOMINANT : BLEND_NORMAL;
		} else if (fk < jg) {
			final boolean dominantGradient = cfg.dominantDirectionThreshold * fk < jg;
			if (ker.j != ker.f && ker.j != ker.k)
				blendResult.j = dominantGradient ? BLEND_DOMINANT : BLEND_NORMAL;

			if (ker.g != ker.f && ker.g != ker.k)
				blendResult.g = dominantGradient ? BLEND_DOMINANT : BLEND_NORMAL;
		}
	}

	/*
	 * input kernel area naming convention:
	 * -------------
	 * | A | B | C |
	 * ----|---|---|
	 * | D | E | F | // input pixel is at position E
	 * ----|---|---|
	 * | G | H | I |
	 * -------------
	 */
	/**
	 * @param rotDeg
	 * @param ker
	 * @param trg
	 * @param trgi
	 * @param trgWidth
	 * @param blendInfo result of preprocessing all four corners of pixel "e"
	 */
	private void scalePixel(final int rotDeg, final Kernel3x3 ker, final OutputMatrix out, final int[] trg, final int trgi, final int trgWidth,
		final char blendInfo) {

		// final int a = ker.data[Rot.data[(0 << 2) + rotDeg]];
		final int b = ker.data[Rotation.data[(1 << 2) + rotDeg]];
		final int c = ker.data[Rotation.data[(2 << 2) + rotDeg]];
		final int d = ker.data[Rotation.data[(3 << 2) + rotDeg]];
		final int e = ker.data[Rotation.data[(4 << 2) + rotDeg]];
		final int f = ker.data[Rotation.data[(5 << 2) + rotDeg]];
		final int g = ker.data[Rotation.data[(6 << 2) + rotDeg]];
		final int h = ker.data[Rotation.data[(7 << 2) + rotDeg]];
		final int i = ker.data[Rotation.data[(8 << 2) + rotDeg]];

		final char blend = BlendInfo.rotate(blendInfo, rotDeg);

		if (BlendInfo.getBottomR(blend) == BLEND_NONE)
			return;

		boolean doLineBlend;

		if (BlendInfo.getBottomR(blend) >= BLEND_DOMINANT)
			doLineBlend = true;

		// make sure there is no second blending in an adjacent
		// rotation for this pixel: handles insular pixels, mario eyes
		// but support double-blending for 90� corners
		else if (BlendInfo.getTopR(blend) != BLEND_NONE && !cmp.compare(e, g))
			doLineBlend = false;

		else if (BlendInfo.getBottomL(blend) != BLEND_NONE && !cmp.compare(e, c))
			doLineBlend = false;

		// no full blending for L-shapes; blend corner only (handles "mario mushroom eyes")
		else if (cmp.compare(g, h) && cmp.compare(h, i) && cmp.compare(i, f) && cmp.compare(f, c) && !cmp.compare(e, i))
			doLineBlend = false;

		else
			doLineBlend = true;

		// choose most similar color
		final int px = cmp.distance(e, f) <= cmp.distance(e, h) ? f : h;

		out.move(rotDeg, trgi);

		if (!doLineBlend) {
			blendCorner(px, out);
			return;
		}

		// test sample: 70% of values max(fg, hc) / min(fg, hc)
		// are between 1.1 and 3.7 with median being 1.9
		final double fg = cmp.distance(f, g);
		final double hc = cmp.distance(h, c);

		final boolean haveShallowLine = cfg.steepDirectionThreshold * fg <= hc && e != g && d != g;
		final boolean haveSteepLine = cfg.steepDirectionThreshold * hc <= fg && e != c && b != c;

		if (haveShallowLine) {
			if (haveSteepLine)
				blendLineSteepAndShallow(px, out);
			else
				blendLineShallow(px, out);
		} else {
			if (haveSteepLine)
				blendLineSteep(px, out);
			else
				blendLineDiagonal(px, out);
		}
	}

	protected void alphaBlend(final int n, final int m, final IntPtr dstPtr, final int col) {
		assert n < 256 : "possible overflow of (col & redMask) * N";
		assert m < 256 : "possible overflow of (col & redMask) * N + (dst & redMask) * (M - N)";
		assert 0 < n && n < m : "0 < N && N < M";
		// this works because 8 upper bits are free
		final int dst = dstPtr.get();
		final int redComponent = blendComponent(redMask, n, m, dst, col);
		final int greenComponent = blendComponent(greenMask, n, m, dst, col);
		final int blueComponent = blendComponent(blueMask, n, m, dst, col);
		final int blend = (redComponent | greenComponent | blueComponent);
		dstPtr.set(blend | 0xff000000);
	}

	private static int blendComponent(final int mask, final int n, final int m, final int inPixel, final int setPixel) {
		final int inChan = inPixel & mask;
		final int setChan = setPixel & mask;
		final int blend = setChan * n + inChan * (m - n);
		final int component = mask & (blend / m);
		return component;
	}

	// fill block with the given color
	private static final void fillBlock(final int[] trg, int trgi, final int pitch, final int col, final int blockSize) {
		for (int y = 0; y < blockSize; ++y, trgi += pitch)
			for (int x = 0; x < blockSize; ++x)
				trg[trgi + x] = col;
	}

	protected abstract int scale();

	protected abstract void blendLineSteep(int col, OutputMatrix out);

	protected abstract void blendLineSteepAndShallow(int col, OutputMatrix out);

	protected abstract void blendLineShallow(int col, OutputMatrix out);

	protected abstract void blendLineDiagonal(int col, OutputMatrix out);

	protected abstract void blendCorner(int col, OutputMatrix out);
}
