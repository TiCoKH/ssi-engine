package ui.resource;

import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;
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
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import data.content.DAXImageContent;
import data.content.WallDef;
import ui.UIResourceLoader;
import ui.UISettings;

public class RenderSurface extends JPanel implements Scrollable {
	private static final long serialVersionUID = -3126585855013388072L;

	private transient UIResourceLoader loader;
	private transient UISettings settings;

	private transient Optional<Object> renderObject = Optional.empty();

	private transient ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	private int index = 0;
	private boolean forward = true;
	private boolean symbols = false;

	public RenderSurface(@Nonnull UIResourceLoader loader, @Nonnull UISettings settings) {
		this.loader = loader;
		this.settings = settings;

		exec.scheduleWithFixedDelay(() -> {
			int picCount = renderObject.map(o -> ((DAXImageContent) o).size()).orElse(1);
			if (forward) {
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
		}, 500, 500, TimeUnit.MILLISECONDS);
	}

	public void changeRenderObject(@Nonnull RenderInfo info) throws IOException {
		index = 0;
		symbols = false;

		if (info.getType() == null) {
			DAXImageContent images;
			if (info.getId() == 201) {
				images = loader.getFont();
			} else if (info.getId() == 201) {
				images = loader.getMisc();
			} else {
				images = loader.findImage(info.getId(), _8X8D);
			}
			renderObject = Optional.ofNullable(images);
			adaptSize(zoom8(16), zoom8(1 + images.size() / 16));
			symbols = true;
			return;
		}
		switch (info.getType()) {
			case BIGPIC:
			case BACK:
			case PIC:
			case SPRIT:
			case TITLE:
				DAXImageContent c = loader.findImage(info.getId(), info.getType());
				renderObject = Optional.ofNullable(c);
				forward = info.getType() == PIC;
				if (info.getType() == SPRIT) {
					index = renderObject.map(o -> 2).orElse(0);
				}
				if (c != null) {
					BufferedImage image = c.get(0);
					adaptSize(zoom(image.getWidth()), zoom(image.getHeight()));
				}
				break;
			case WALLDEF:
				WallResources wallRes = new WallResources();
				wallRes.walls = loader.find(info.getId(), WallDef.class, WALLDEF);
				wallRes.wallSymbols = loader.findImage(info.getId(), _8X8D).toList();
				renderObject = Optional.ofNullable(wallRes);
				adaptSize(zoom8(22), zoom8(11 * wallRes.walls.getWallCount()));
				break;
			default:
				clearRenderObject();
		}
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
			if (o instanceof DAXImageContent && symbols) {
				DAXImageContent ic = (DAXImageContent) o;
				for (int i = 0; i < ic.size(); i++) {
					BufferedImage image = ic.get(i);
					int x = i % 16;
					int y = i / 16;
					g2d.drawImage(image.getScaledInstance(zoom(image.getWidth()), zoom(image.getHeight()), 0), zoom8(x), zoom8(y), null);
				}
			} else if (o instanceof DAXImageContent) {
				DAXImageContent ic = (DAXImageContent) o;
				BufferedImage image = ic.get(index);
				g2d.drawImage(image.getScaledInstance(zoom(image.getWidth()), zoom(image.getHeight()), 0), 0, 0, null);
			} else if (o instanceof WallResources) {
				WallResources wallRes = (WallResources) o;
				WallDef walls = wallRes.walls;
				List<BufferedImage> wallSymbols = wallRes.wallSymbols;

				g2d.setBackground(Color.BLUE);
				g2d.clearRect(0, 0, getWidth(), getHeight());

				for (int i = 0; i < wallRes.walls.getWallCount(); i++) {
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, CLOSE, LEFT), i, 0, 0);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, CLOSE, FOWARD), i, 2, 1);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, MEDIUM, LEFT), i, 9, 1);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, MEDIUM, FOWARD), i, 11, 3);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, FAR, LEFT), i, 14, 3);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, FAR, FOWARD), i, 15, 4);
					drawWallView(g2d, wallSymbols, walls.getWallDisplayFarFiller(i), i, 16, 4);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, FAR, RIGHT), i, 17, 3);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, MEDIUM, RIGHT), i, 18, 1);
					drawWallView(g2d, wallSymbols, walls.getWallDisplay(i, CLOSE, RIGHT), i, 20, 0);
				}
			}
		});
	}

	private void drawWallView(Graphics2D g2d, List<BufferedImage> wallSymbols, int[][] wallView, int wallIndex, int xStart, int yStart) {
		for (int y = 0; y < wallView.length; y++) {
			int[] row = wallView[y];
			for (int x = 0; x < row.length; x++) {
				BufferedImage image = wallSymbols.get(row[x]);
				g2d.drawImage(image.getScaledInstance(zoom(image.getWidth()), zoom(image.getHeight()), 0), zoom8(xStart + x),
					zoom8(11 * wallIndex + yStart + y), null);
			}
		}
	}

	private void adaptSize(int width, int height) {
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getMinimumSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return zoom8(1);
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
		return zoom8(1);
	}

	protected int zoom(int pos) {
		return settings.getZoom() * pos;
	}

	protected int zoom8(int pos) {
		return settings.getZoom() * 8 * pos;
	}

	private static class WallResources {
		WallDef walls;
		List<BufferedImage> wallSymbols;
	}
}
