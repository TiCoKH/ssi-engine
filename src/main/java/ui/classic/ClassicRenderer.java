package ui.classic;

import static ui.BorderSymbols.EM;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import engine.RendererCallback;
import engine.opcodes.EclString;
import ui.BorderSymbols;
import ui.Borders;

public class ClassicRenderer extends JPanel {
	private static final int TEXT_START_X = 1;
	private static final int TEXT_START_Y = 17;
	private static final int TEXT_LINE_WIDTH = 38;
	// Order is FORWARD(FAR,MEDIUM,CLOSE), LEFT(FAR,MEDIUM,CLOSE), RIGHT(FAR,MEDIUM,CLOSE)
	private static final int[] WALL_START_X = { 8, 7, 5, 6, 5, 3, 9, 10, 12 };
	private static final int[] WALL_START_Y = { 7, 6, 4, 6, 4, 3, 6, 4, 3 };

	private RendererCallback renderCB;

	private MonocromeSymbols font;
	private DAXImageContent borderSymbols;

	private int zoom;

	private BufferedImage title;

	private EclString statusLine;

	private Borders layout;
	private PictureType picType;
	private DAXImageContent pic;
	private int picIndex;

	private EclString text;
	private int textPos;
	private boolean textIsRendering;

	private List<BufferedImage> backdrops;
	private List<BufferedImage> wallSymbols;

	public ClassicRenderer(RendererCallback renderCB, MonocromeSymbols font, DAXImageContent borderSymbols) {
		this.renderCB = renderCB;
		this.font = font;
		this.borderSymbols = borderSymbols;

		this.zoom = 4;

		this.title = null;

		this.layout = null;
		this.picType = null;
		this.pic = null;
		this.picIndex = -1;

		this.text = null;
		this.textPos = 0;
		this.textIsRendering = false;

		this.backdrops = null;
		this.wallSymbols = null;

		initRenderer();
	}

	public void setStatusLine(EclString statusLine) {
		this.statusLine = statusLine;
	}

	public void setTitleScreen(BufferedImage title) {
		this.layout = null;
		this.title = title;
	}

	public void setDungeonDisplay(List<BufferedImage> backdrops, List<BufferedImage> wallSymbols) {
		this.layout = Borders.GAME;
		this.backdrops = backdrops;
		this.wallSymbols = wallSymbols;
	}

	public void setNoPicture(Borders b) {
		this.layout = b;
		this.picType = null;
	}

	public void setBigPicture(DAXImageContent pic, int picIndex) {
		this.layout = Borders.BIGPIC;
		this.picType = PictureType.BIG;
		this.pic = pic;
		this.picIndex = picIndex;
	}

	public void setSmallPicture(DAXImageContent pic, int picIndex) {
		this.layout = Borders.GAME;
		this.picType = PictureType.SMALL;
		this.pic = pic;
		this.picIndex = picIndex;
	}

	public void setText(EclString text) {
		this.text = text;
		this.textPos = 0;
		this.textIsRendering = true;
	}

	public void increaseText() {
		if (this.text != null) {
			if (this.textPos == this.text.getLength()) {
				if (this.textIsRendering) {
					this.textIsRendering = false;
					this.renderCB.textDisplayFinished();
				}
			} else {
				this.textPos++;
			}
		}
	}

	private void initRenderer() {
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(320 * zoom, 200 * zoom));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.BLACK);
		g2d.clearRect(0, 0, 320 * zoom, 200 * zoom);

		if (statusLine != null) {
			renderStatus(g2d);
		}
		if (title != null) {
			renderTitle(g2d);
			return;
		}
		if (layout != null) {
			renderBorders(g2d);
		}
		if (picType != null && pic.size() > picIndex) {
			renderPicture(g2d);
		} else if (backdrops != null && wallSymbols != null) {
			renderBackdrop(g2d);
			renderDungeon(g2d);
		}
		if (text != null) {
			renderText(g2d);
		}
	}

	private void renderStatus(Graphics2D g2d) {
		for (int pos = 0; pos < statusLine.getLength(); pos++) {
			int x = zoom8(pos);
			int y = zoom8(24);
			BufferedImage c = font.get(statusLine.getChar(pos));
			g2d.drawImage(c.getScaledInstance(zoom(c.getWidth()), zoom(c.getHeight()), 0), x, y, null);
		}
	}

	private void renderTitle(Graphics2D g2d) {
		int x = zoom((320 - title.getWidth()) / 2);
		int y = zoom((200 - title.getHeight()) / 2);
		g2d.drawImage(title.getScaledInstance(zoom(title.getWidth()), zoom(title.getHeight()), 0), x, y, null);
	}

	private void renderBorders(Graphics2D g2d) {
		for (int y = 0; y < 24; y++) {
			BorderSymbols[] row = layout.getSymbols()[y];
			for (int x = 0; x < 40; x++) {
				if (x >= row.length || row[x] == EM) {
					continue;
				}
				BufferedImage s = borderSymbols.get(row[x].getIndex());
				g2d.drawImage(s.getScaledInstance(zoom(s.getWidth()), zoom(s.getHeight()), 0), zoom8(x), zoom8(y), null);
			}
		}
	}

	private void renderPicture(Graphics2D g2d) {
		int x = zoom8(picType == PictureType.SMALL ? 3 : 1);
		BufferedImage p = pic.get(picIndex);
		g2d.drawImage(p.getScaledInstance(zoom(p.getWidth()), zoom(p.getHeight()), 0), x, x, null);
	}

	private void renderText(Graphics2D g2d) {
		for (int pos = 0; pos < textPos; pos++) {
			int x = zoom8(TEXT_START_X + (pos % TEXT_LINE_WIDTH));
			int y = zoom8(TEXT_START_Y + (pos / TEXT_LINE_WIDTH));
			BufferedImage c = font.get(text.getChar(pos));
			g2d.drawImage(c.getScaledInstance(zoom(c.getWidth()), zoom(c.getHeight()), 0), x, y, null);
		}
	}

	private void renderBackdrop(Graphics2D g2d) {
		int x = zoom8(3);
		BufferedImage bd = backdrops.get(0);
		g2d.drawImage(bd.getScaledInstance(zoom(bd.getWidth()), zoom(bd.getHeight()), 0), x, x, null);
	}

	private void renderDungeon(Graphics2D g2d) {
		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				int[][] wd = renderCB.getWallDisplay(dis, plc);
				if (wd != null) {
					int xStart = WALL_START_X[3 * plc.ordinal() + dis.ordinal()];
					int yStart = WALL_START_Y[3 * plc.ordinal() + dis.ordinal()];
					renderWall(g2d, wd, xStart, yStart);
				}
			}
		}
	}

	private void renderWall(Graphics2D g2d, int[][] wallDisplay, int xStart, int yStart) {
		for (int y = 0; y < wallDisplay.length; y++) {
			int[] row = wallDisplay[y];
			for (int x = 0; x < row.length; x++) {
				int xPos = zoom8(xStart + x);
				int yPos = zoom8(yStart + y);
				BufferedImage w = wallSymbols.get(row[x]);
				g2d.drawImage(w.getScaledInstance(zoom(w.getWidth()), zoom(w.getHeight()), 0), xPos, yPos, null);
			}
		}
	}

	private int zoom(int pos) {
		return zoom * pos;
	}

	private int zoom8(int pos) {
		return zoom * 8 * pos;
	}

	private enum PictureType {
		SMALL, BIG;
	}
}
