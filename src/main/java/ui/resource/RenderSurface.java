package ui.resource;

import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
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

import data.content.WallDef;
import ui.DungeonResource;
import ui.FontType;
import ui.ImageResource;
import ui.UIResourceManager;
import ui.UISettings;

public class RenderSurface extends JPanel implements Scrollable {
	private static final long serialVersionUID = -3126585855013388072L;

	private transient UIResourceManager resman;
	private transient UISettings settings;

	private transient Optional<Object> renderObject = Optional.empty();

	private transient ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

	private int index = 0;

	public RenderSurface(@Nonnull UIResourceManager resman, @Nonnull UISettings settings) {
		this.resman = resman;
		this.settings = settings;

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
		else
			clearRenderObject();
	}

	public void changeRenderObject(@Nonnull ImageResource ir) {
		index = ir.getType() == SPRIT ? 2 : 0;
		renderObject = Optional.of(ir);
	}

	public void changeRenderObject(@Nonnull DungeonResource dr) {
		WallDef walls = resman.getWallResource(dr);
		renderObject = Optional.of(dr);
		adaptSize(settings.zoom8(22), settings.zoom8(11 * walls.getWallCount()));
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
				if (ir.getType() == null) {
					List<BufferedImage> images = null;
					if (ir.getId() == 201)
						images = resman.getFont(FontType.NORMAL);
					else if (ir.getId() == 202)
						images = resman.getMisc();
					adaptSize(settings.zoom8(16), settings.zoom8(1 + images.size() / 16));
					for (int i = 0; i < images.size(); i++) {
						g2d.drawImage(images.get(i), settings.zoom8(i % 16), settings.zoom8(i / 16), null);
					}
				} else {
					BufferedImage image = resman.getImageResource(ir).get(index);
					adaptSize(image.getWidth(), image.getHeight());
					g2d.drawImage(image, 0, 0, null);
				}
			} else if (o instanceof DungeonResource) {
				DungeonResource res = (DungeonResource) o;
				WallDef walls = resman.getWallResource(res);
				List<BufferedImage> wallSymbols = resman.getImageResource(res);

				g2d.setBackground(Color.BLUE);
				g2d.clearRect(0, 0, getWidth(), getHeight());

				for (int i = 0; i < walls.getWallCount(); i++) {
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
				g2d.drawImage(wallSymbols.get(row[x]), settings.zoom8(xStart + x), settings.zoom8(11 * wallIndex + yStart + y), null);
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
