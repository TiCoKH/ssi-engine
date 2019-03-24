package ui;

import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static ui.ScaleMethod.BICUBIC;
import static ui.ScaleMethod.BILINEAR;
import static ui.ScaleMethod.XBRZ;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

import javax.annotation.Nonnull;

import common.scaler.xbrz.Scaler;
import common.scaler.xbrz.Scaler2x;
import common.scaler.xbrz.Scaler3x;
import common.scaler.xbrz.Scaler4x;
import common.scaler.xbrz.Scaler5x;
import common.scaler.xbrz.ScalerConfig;

public class UIScaler {
	private UISettings settings;
	private Scaler xBRZ;

	public UIScaler(@Nonnull UISettings settings) {
		this.settings = settings;
		this.xBRZ = createXBRZScaler();
	}

	@Nonnull
	public BufferedImage scale(@Nonnull BufferedImage image) {
		final int srcWidth = image.getWidth();
		final int srcHeight = image.getHeight();
		final int targetWidth = settings.zoom(srcWidth);
		final int targetHeight = settings.zoom(srcHeight);

		BufferedImage scaled = createTarget(image, targetWidth, targetHeight);
		if (settings.getMethod() == XBRZ) {
			int[] src = new int[srcWidth * srcHeight];
			image.getRGB(0, 0, srcWidth, srcHeight, src, 0, srcWidth);

			int[] target = new int[targetWidth * targetHeight];
			xBRZ.scaleImage(src, target, srcWidth, srcHeight, 0, srcWidth);

			scaled.setRGB(0, 0, targetWidth, targetHeight, target, 0, targetWidth);
		} else {
			Graphics2D g2d = scaled.createGraphics();
			if (settings.getMethod() == BILINEAR) {
				g2d.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BILINEAR);
			} else if (settings.getMethod() == BICUBIC) {
				g2d.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
			}
			g2d.drawImage(image, 0, 0, settings.zoom(image.getWidth()), settings.zoom(image.getHeight()), //
				0, 0, image.getWidth(), image.getHeight(), null);
			g2d.dispose();
		}
		return scaled;
	}

	@Nonnull
	private static BufferedImage createTarget(@Nonnull BufferedImage source, int targetWidth, int targetHeight) {
		ColorModel cm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, //
			0xff0000, 0x00ff00, 0x0000ff, 0xff000000, true, DataBuffer.TYPE_INT);
		WritableRaster r = cm.createCompatibleWritableRaster(targetWidth, targetHeight);

		// Copy properties from source
		Hashtable<String, Object> props = null;
		String[] names = source.getPropertyNames();
		if (names != null) {
			props = new Hashtable<>();
			for (int i = 0; i < names.length; i++) {
				props.put(names[i], source.getProperty(names[i]));
			}
		}

		return new BufferedImage(cm, r, true, props);
	}

	private Scaler createXBRZScaler() {
		ScalerConfig cfg = new ScalerConfig();
		switch (settings.getZoom()) {
			case 2:
				return new Scaler2x(cfg);
			case 3:
				return new Scaler3x(cfg);
			case 4:
				return new Scaler4x(cfg);
			case 5:
				return new Scaler5x(cfg);
			default:
				throw new IllegalArgumentException("Scale factor " + settings.getZoom() + " is not valid for xBRZ");
		}
	}
}
