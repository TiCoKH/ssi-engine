package ui.classic;

import static data.ContentType._8X8D;
import static data.image.ImageContentProperties.X_OFFSET;
import static data.image.ImageContentProperties.Y_OFFSET;
import static ui.FrameType.FRAME;
import static ui.FrameType.PortraitType.NORMAL;
import static ui.FrameType.PortraitType.THICK;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ui.FrameType.BackgroundType;
import ui.FrameType.PortraitType;
import ui.ImageResource;
import ui.UIFrame;
import ui.UIResourceConfiguration;
import ui.UIResourceManager;
import ui.UISettings;

public class FrameRenderer {
	private UIResourceConfiguration config;
	private UIResourceManager resman;
	private UISettings settings;

	private boolean portraitShown = true;;

	public FrameRenderer(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman, @Nonnull UISettings settings) {
		this.config = config;
		this.resman = resman;
		this.settings = settings;
	}

	public boolean isPortraitShown() {
		return portraitShown;
	}

	public void setPortraitShown(boolean portraitShown) {
		this.portraitShown = portraitShown;
	}

	public void render(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		if (!UIFrame.NONE.equals(f)) {
			drawBackground(g2d);
		} else {
			g2d.setBackground(Color.black);
			g2d.clearRect(0, 0, settings.zoom(320), settings.zoom(200));
		}
		if (FRAME.equals(config.getFrameType())) {
			renderFrameTypeFrame(g2d, f);
		} else {
			renderSymbolsTypeFrame(g2d, f);
		}
	}

	private void drawBackground(@Nonnull Graphics2D g2d) {
		Color c = Color.black;
		if (BackgroundType.COLOR.equals(config.getBackgroundType())) {
			c = Color.decode(config.getBackground());
		}
		g2d.setBackground(c);
		g2d.clearRect(0, 0, settings.zoom(320), settings.zoom(200));
		if (BackgroundType.IMAGE.equals(config.getBackgroundType())) {
			drawImageFrameBorder(g2d, config.getBackground(), 0, 0);
		}
	}

	private void renderFrameTypeFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		if (!UIFrame.NONE.equals(f)) {
			drawFramePart(g2d, parseFrameIndexes(config.getOuterFrameTop(f)));
			drawFramePart(g2d, parseFrameIndexes(config.getOuterFrameBottom(f)));
			drawFramePart(g2d, parseFrameIndexes(config.getOuterFrameLeft(f)));
			drawFramePart(g2d, parseFrameIndexes(config.getOuterFrameRight(f)));
		}

		if (UIFrame.BIGPIC.equals(f)) {
			drawFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 128);
		}
		if (UIFrame.GAME.equals(f)) {
			drawFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 96);
			drawFramePart(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 0, 128);
			drawFramePart(g2d, parseFrameIndexes(config.getPortraitTop()));
			drawFramePart(g2d, parseFrameIndexes(config.getPortraitLeft()));
		}
	}

	private void renderSymbolsTypeFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		switch (f) {
			case GAME:
			case SPACE:
				boolean isThickPortrait = PortraitType.THICK.equals(config.getPortraitType());

				drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameTop(f)), isThickPortrait ? 17 : 0, 0);
				drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameBottom(f)), 0, 23);
				drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameLeft(f)), 0, isThickPortrait ? 17 : 1);
				drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameRight(f)), 39, 1);

				drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), isThickPortrait ? 17 : 1, 16);

				if (!isThickPortrait)
					drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameVertical(f)), 16, 1);

				if (isPortraitShown())
					drawPortrait(g2d);
				break;
			case BIGPIC:
				drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getInnerFrameHorizontal(f)), 1, 16);
			case SCREEN:
				drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameTop(f)), 0, 0);
				drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameBottom(f)), 0, 23);
				drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameLeft(f)), 0, 1);
				drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getOuterFrameRight(f)), 39, 1);
				break;
			default:
				break;
		}
	}

	private void drawPortrait(@Nonnull Graphics2D g2d) {
		PortraitType type = config.getPortraitType();
		if (NORMAL.equals(type)) {
			drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getPortraitTop()), 2, 2);
			drawHorizontalFrameBorder(g2d, parseFrameIndexes(config.getPortraitBottom()), 2, 14);
			drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getPortraitLeft()), 2, 3);
			drawVerticalFrameBorder(g2d, parseFrameIndexes(config.getPortraitRight()), 14, 3);
		} else if (THICK.equals(type)) {
			drawImageFrameBorder(g2d, config.getPortraitTop(), 0, 0);
			drawImageFrameBorder(g2d, config.getPortraitBottom(), 0, 14);
			drawImageFrameBorder(g2d, config.getPortraitLeft(), 0, 3);
			drawImageFrameBorder(g2d, config.getPortraitRight(), 14, 3);
		}
	}

	private void drawHorizontalFrameBorder(Graphics2D g2d, int[] indexes, int startX, int y) {
		List<BufferedImage> frames = resman.getFrames();
		for (int x = 0; x < indexes.length; x++) {
			if (indexes[x] != -1) {
				g2d.drawImage(frames.get(indexes[x]), settings.zoom8(startX + x), settings.zoom8(y), null);
			}
		}
	}

	private void drawVerticalFrameBorder(Graphics2D g2d, int[] indexes, int x, int startY) {
		List<BufferedImage> frames = resman.getFrames();
		for (int y = 0; y < indexes.length; y++) {
			if (indexes[y] != -1) {
				g2d.drawImage(frames.get(indexes[y]), settings.zoom8(x), settings.zoom8(startY + y), null);
			}
		}
	}

	private void drawImageFrameBorder(Graphics2D g2d, String source, int x, int y) {
		StringTokenizer st = new StringTokenizer(source, ",");
		String filename = st.nextToken();
		int blockId = Integer.parseUnsignedInt(st.nextToken());
		int index = Integer.parseUnsignedInt(st.nextToken());

		ImageResource res = new ImageResource(filename, blockId, _8X8D);
		g2d.drawImage(resman.getImageResource(res).get(index), settings.zoom8(x), settings.zoom8(y), null);
	}

	private void drawFramePart(Graphics2D g2d, int[] indexes) {
		List<BufferedImage> frames = resman.getFrames();
		for (int i = 0; i < indexes.length; i++) {
			BufferedImage part = frames.get(indexes[i]);
			int x = Math.abs((int) part.getProperty(X_OFFSET.name()));
			int y = Math.abs((int) part.getProperty(Y_OFFSET.name()));
			g2d.drawImage(part, settings.zoom(x), settings.zoom(y), null);
		}
	}

	private void drawFramePart(Graphics2D g2d, int[] indexes, int x, int y) {
		List<BufferedImage> frames = resman.getFrames();
		for (int i = 0; i < indexes.length; i++) {
			BufferedImage part = frames.get(indexes[i]);
			g2d.drawImage(part, settings.zoom(x), settings.zoom(y), null);
		}
	}

	private int[] parseFrameIndexes(String indexes) {
		return Stream.of(indexes.split(",")).mapToInt(Integer::parseInt).toArray();
	}
}
