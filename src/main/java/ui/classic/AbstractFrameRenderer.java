package ui.classic;

import static data.ContentType._8X8D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import ui.UISettings;
import ui.shared.FrameType.BackgroundType;
import ui.shared.UIFrame;
import ui.shared.resource.ImageResource;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;

public abstract class AbstractFrameRenderer {
	protected UIResourceConfiguration config;
	protected UIResourceManager resman;
	protected UISettings settings;

	private boolean portraitShown = true;;

	public AbstractFrameRenderer(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman, @Nonnull UISettings settings) {
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

	public void render(Graphics2D g2d, UIFrame f) {
		renderBackground(g2d, f);
		renderFrame(g2d, f);
	}

	public void clearStatusLine(Graphics2D g2d) {
		Color c = Color.black;
		if (BackgroundType.COLOR.equals(config.getBackgroundType())) {
			c = Color.decode(config.getBackground());
		}
		g2d.setBackground(c);
		g2d.clearRect(0, settings.zoom8(24), settings.zoom(320), settings.zoom(200));
	}

	protected abstract void renderFrame(@Nonnull Graphics2D g2d, @Nonnull UIFrame f);

	protected void renderBackground(@Nonnull Graphics2D g2d, @Nonnull UIFrame f) {
		if (UIFrame.NONE.equals(f)) {
			g2d.setBackground(Color.black);
			g2d.clearRect(0, 0, settings.zoom(320), settings.zoom(200));
			return;
		}

		Color c = Color.black;
		if (BackgroundType.COLOR.equals(config.getBackgroundType())) {
			c = Color.decode(config.getBackground());
		}
		g2d.setBackground(c);
		g2d.clearRect(0, 0, settings.zoom(320), settings.zoom(200));
		if (BackgroundType.IMAGE.equals(config.getBackgroundType())) {
			renderImage(g2d, config.getBackground(), 0, 0);
		}
	}

	protected void renderImage(Graphics2D g2d, String source, int x, int y) {
		StringTokenizer st = new StringTokenizer(source, ",");
		String filename = st.nextToken();
		int blockId = Integer.parseUnsignedInt(st.nextToken());
		int index = Integer.parseUnsignedInt(st.nextToken());

		ImageResource res = new ImageResource(filename, blockId, _8X8D);
		g2d.drawImage(resman.getImageResource(res).get(index), settings.zoom8(x), settings.zoom8(y), null);
	}

	protected int[] parseFrameIndexes(String indexes) {
		if (Strings.isNullOrEmpty(indexes)) {
			return new int[0];
		}
		return Stream.of(indexes.split(",")).mapToInt(Integer::parseInt).toArray();
	}
}
