package ui.shared.resource;

import static data.ContentType.BIGPIC;
import static data.ContentType.BODY;
import static data.ContentType.HEAD;
import static data.ContentType.PIC;
import static data.ContentType.TITLE;
import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR;
import static ui.UISettings.ScaleMethod.BICUBIC;
import static ui.UISettings.ScaleMethod.BILINEAR;
import static ui.UISettings.ScaleMethod.XBRZ;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import common.scaler.xbrz.Scaler;
import common.scaler.xbrz.Scaler2x;
import common.scaler.xbrz.Scaler3x;
import common.scaler.xbrz.Scaler4x;
import common.scaler.xbrz.Scaler5x;
import common.scaler.xbrz.ScalerConfig;
import data.ContentType;
import data.image.ImageContentProperties;
import ui.UISettings;
import ui.UISettings.PropertyName;

public class UIScaler {
	private UISettings settings;
	private Scaler xBRZ;

	public UIScaler(@Nonnull UISettings settings) {
		this.settings = settings;
		this.settings.addPropertyChangeListener(e -> {
			if (e.getPropertyName() == PropertyName.ZOOM.name())
				this.xBRZ = createXBRZScaler();
		});
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
			xBRZ.scaleImage(src, target, srcWidth, srcHeight, 0, srcHeight);

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
	public BufferedImage scaleComposite(@Nonnull ContentType type, @Nonnull List<BufferedImage> images, List<Point> offsets) {
		int width = 0, height = 0;
		if (type == TITLE) {
			width = 320;
			height = 200;
		} else if (type == BIGPIC) {
			width = 304;
			height = 120;
		} else if (type == PIC || type == BODY || type == HEAD) {
			width = height = 88;
		}
		BufferedImage composite = createTarget(null, width, height);
		Graphics2D g2d = composite.createGraphics();

		boolean useExternalOffsets = images.size() == 1 || images.stream().noneMatch(UIScaler::isOffsetNotZero);
		for (int i = 0; i < images.size(); i++) {
			BufferedImage image = images.get(i);
			if (useExternalOffsets) {
				Point offset = offsets.get(i);
				g2d.drawImage(image, (int) offset.getX(), (int) offset.getY(), null);
			} else {
				int xStart = Math.abs((int) image.getProperty(X_OFFSET.name()));
				int yStart = Math.abs((int) image.getProperty(Y_OFFSET.name()));
				g2d.drawImage(image, xStart, yStart, null);
			}
		}
		return scale(composite);
	}

	private static boolean isOffsetNotZero(@Nonnull BufferedImage image) {
		int xStart = (int) image.getProperty(X_OFFSET.name());
		int yStart = (int) image.getProperty(Y_OFFSET.name());
		return xStart != 0 || yStart != 0;
	}

	@Nonnull
	private static BufferedImage createTarget(@Nullable BufferedImage source, int targetWidth, int targetHeight) {
		ColorModel cm = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), 32, //
			0xff0000, 0x00ff00, 0x0000ff, 0xff000000, true, DataBuffer.TYPE_INT);
		WritableRaster r = cm.createCompatibleWritableRaster(targetWidth, targetHeight);

		// Copy properties from source
		Hashtable<String, Object> props = new Hashtable<>();
		if (source != null && source.getPropertyNames() != null) {
			String[] names = source.getPropertyNames();
			for (int i = 0; i < names.length; i++) {
				props.put(names[i], source.getProperty(names[i]));
			}
		} else {
			for (ImageContentProperties p : ImageContentProperties.values()) {
				props.put(p.name(), 0);
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
