package ui.resource;

import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType._8X8D;
import static data.content.ImageContentProperties.X_OFFSET;
import static data.content.ImageContentProperties.Y_OFFSET;
import static data.content.WallDef.WallDistance.CLOSE;
import static data.content.WallDef.WallDistance.FAR;
import static data.content.WallDef.WallDistance.MEDIUM;
import static data.content.WallDef.WallPlacement.FOWARD;
import static data.content.WallDef.WallPlacement.LEFT;
import static data.content.WallDef.WallPlacement.RIGHT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import data.content.DAXContentType;
import ui.DungeonMapResource;
import ui.DungeonResource;
import ui.DungeonWall;
import ui.FontType;
import ui.ImageCompositeResource;
import ui.ImageResource;
import ui.UIFrame;
import ui.UIResourceConfiguration;
import ui.UIResourceManager;
import ui.UISettings;
import ui.classic.FrameRenderer;

public class RenderSurface extends JPanel implements Scrollable {
	private static final long serialVersionUID = -3126585855013388072L;

	private transient UIResourceManager resman;
	private transient UISettings settings;

	private transient FrameRenderer frameRenderer;

	private transient Optional<Object> renderObject = Optional.empty();

	private transient ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	private int index = 0;

	public RenderSurface(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman, @Nonnull UISettings settings) {
		this.resman = resman;
		this.settings = settings;
		this.frameRenderer = new FrameRenderer(config, resman, settings);

		exec.scheduleWithFixedDelay(() -> {
			ImageResource ir = renderObject //
				.filter(o -> o instanceof ImageResource) //
				.map(o -> (ImageResource) o) //
				.filter(o -> o.getType() == PIC || o.getType() == SPRIT) //
				.orElse(null);
			if (ir != null) {
				int picCount = resman.getImageResource(ir).size();
				if (ir.getType() == PIC) {
					if (index + 1 == picCount) {
						index = 0;
					} else {
						index++;
					}
				} else {
					if (index == 0) {
						index = picCount - 1;
					} else {
						index--;
					}
				}
				repaint();
			}
		}, 500, 500, TimeUnit.MILLISECONDS);
	}

	public void changeRenderObject(@Nonnull Object o) {
		if (o instanceof ImageResource)
			changeRenderObject((ImageResource) o);
		else if (o instanceof DungeonResource)
			changeRenderObject((DungeonResource) o);
		else if (o instanceof DungeonMapResource)
			changeRenderObject((DungeonMapResource) o);
		else if (o instanceof UIFrame)
			changeRenderObject((UIFrame) o);
		else
			clearRenderObject();
	}

	public void changeRenderObject(@Nonnull ImageResource ir) {
		if (ir.getType() == DAXContentType.TITLE) {
			index = 0;
			renderObject = Optional.of(new ImageCompositeResource(ir));
		} else {
			index = ir.getType() == SPRIT ? 2 : 0;
			renderObject = Optional.of(ir);
		}
	}

	public void changeRenderObject(@Nonnull DungeonResource dr) {
		List<DungeonWall> walls = resman.getWallResource(dr);
		renderObject = Optional.of(dr);
		adaptSize(settings.zoom8(22), settings.zoom8(11 * walls.size()));
	}

	public void changeRenderObject(@Nonnull UIFrame frame) {
		renderObject = Optional.of(frame);
		adaptSize(settings.zoom8(40), settings.zoom8(25));
	}

	public void changeRenderObject(@Nonnull DungeonMapResource mr) {
		renderObject = Optional.of(mr);
		adaptSize(settings.zoom8(mr.getMapWidth()), settings.zoom8(mr.getMapHeight()));
	}

	public void clearRenderObject() {
		renderObject = Optional.empty();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.BLACK);
		g2d.clearRect(0, 0, getWidth(), getHeight());

		renderObject.ifPresent(o -> {
			if (o instanceof ImageResource) {
				ImageResource ir = (ImageResource) o;
				if (ir.getType() == null || ir.getType() == _8X8D) {
					List<BufferedImage> images = null;
					if (ir.getId() == 201)
						images = resman.getFont(FontType.NORMAL);
					else if (ir.getId() == 202)
						images = resman.getMisc();
					else
						images = resman.getImageResource(ir);
					adaptSize(settings.zoom8(16), settings.zoom8(1 + images.size() / 16));
					for (int i = 0; i < images.size(); i++) {
						g2d.drawImage(images.get(i), settings.zoom8(i % 16), settings.zoom8(i / 16), null);
					}
				} else {
					BufferedImage image = resman.getImageResource(ir).get(index);
					int xOffset = settings.zoom(Math.abs((int) image.getProperty(X_OFFSET.name())));
					int yOffset = settings.zoom(Math.abs((int) image.getProperty(Y_OFFSET.name())));
					adaptSize(image.getWidth() + xOffset, image.getHeight() + yOffset);
					g2d.drawImage(image, xOffset, yOffset, null);
				}
			} else if (o instanceof DungeonResource) {
				DungeonResource res = (DungeonResource) o;

				List<DungeonWall> walls = resman.getWallResource(res);

				g2d.setBackground(Color.BLUE);
				g2d.clearRect(0, 0, getWidth(), getHeight());

				int i = 0;
				for (DungeonWall wall : walls) {
					drawWallView(g2d, wall.getWallViewFor(CLOSE, LEFT), i, 0, 0, 88);
					drawWallView(g2d, wall.getWallViewFor(CLOSE, FOWARD), i, 2, 1, 64);
					drawWallView(g2d, wall.getWallViewFor(MEDIUM, LEFT), i, 9, 1, 64);
					drawWallView(g2d, wall.getWallViewFor(MEDIUM, FOWARD), i, 11, 3, 32);
					drawWallView(g2d, wall.getWallViewFor(FAR, LEFT), i, 14, 3, 32);
					drawWallView(g2d, wall.getWallViewFor(FAR, FOWARD), i, 15, 4, 16);
					drawWallView(g2d, wall.getFarFiller(), i, 16, 4, 16);
					drawWallView(g2d, wall.getWallViewFor(FAR, RIGHT), i, 17, 3, 32);
					drawWallView(g2d, wall.getWallViewFor(MEDIUM, RIGHT), i, 18, 1, 64);
					drawWallView(g2d, wall.getWallViewFor(CLOSE, RIGHT), i, 20, 0, 88);
					i++;
				}
			} else if (o instanceof DungeonMapResource) {
				g2d.drawImage(resman.getMapResource((DungeonMapResource) o), 0, 0, null);
			} else if (o instanceof UIFrame) {
				frameRenderer.render(g2d, (UIFrame) o);
			}
		});
	}

	private void drawWallView(Graphics2D g2d, BufferedImage image, int wallIndex, int xStart, int yStart, int maxHeight) {
		g2d.drawImage(image, settings.zoom8(xStart), settings.zoom8(11 * wallIndex + yStart) + settings.zoom(maxHeight) - image.getHeight(), null);
	}

	private void adaptSize(int width, int height) {
		Dimension d = getMinimumSize();
		int newWidth = width > d.width ? width : d.width;
		int newHeight = height > d.height ? height : d.height;
		setMinimumSize(new Dimension(newWidth, newHeight));
		setPreferredSize(new Dimension(newWidth, newHeight));
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getMinimumSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return settings.zoom8(1);
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return settings.zoom8(1);
	}
}
