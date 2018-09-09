package ui.classic;

import static ui.BorderSymbols.EM;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import engine.RendererCallback;
import engine.opcodes.EclString;
import ui.BorderSymbols;
import ui.Borders;

public class ClassicRenderer extends JPanel {
	private static final int TEXT_START_X = 1;
	private static final int TEXT_START_Y = 17;
	private static final int TEXT_LINE_WIDTH = 38;

	private RendererCallback renderCB;

	private MonocromeSymbols font;
	private DAXImageContent borderSymbols;

	private int zoom;
	private Borders layout;
	private PictureType picType;
	private DAXImageContent pic;
	private int picIndex;

	private EclString text;
	private int textPos;
	private boolean textIsRendering;

	public ClassicRenderer(RendererCallback renderCB, MonocromeSymbols font, DAXImageContent borderSymbols) {
		this.renderCB = renderCB;
		this.font = font;
		this.borderSymbols = borderSymbols;

		this.zoom = 2;
		this.layout = null;
		this.picType = null;
		this.pic = null;
		this.picIndex = -1;

		this.text = null;
		this.textPos = 0;
		this.textIsRendering = false;
		initRenderer();
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
		setPreferredSize(new Dimension(640, 400));
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.BLACK);
		g2d.clearRect(0, 0, 640, 400);

		if (layout != null) {
			renderBorders(g2d);
		}
		if (picType != null && pic.size() > picIndex) {
			renderPicture(g2d);
		}
		if (text != null) {
			renderText(g2d);
		}
	}

	private void renderBorders(Graphics2D g2d) {
		for (int y = 0; y < 24; y++) {
			BorderSymbols[] row = layout.getSymbols()[y];
			for (int x = 0; x < 40; x++) {
				if (x >= row.length || row[x] == EM) {
					continue;
				}
				BufferedImage s = borderSymbols.get(row[x].getIndex());
				g2d.drawImage(s.getScaledInstance(s.getWidth() * zoom, s.getHeight() * zoom, 0), zoom * 8 * x, zoom * 8 * y, null);
			}
		}
	}

	private void renderPicture(Graphics2D g2d) {
		int x = zoom * 8 * (picType == PictureType.SMALL ? 3 : 1);
		BufferedImage image = pic.get(picIndex);
		g2d.drawImage(image.getScaledInstance(image.getWidth() * zoom, image.getHeight() * zoom, 0), x, x, null);
	}

	private void renderText(Graphics2D g2d) {
		for (int pos = 0; pos < textPos; pos++) {
			int x = zoom * 8 * (TEXT_START_X + (pos % TEXT_LINE_WIDTH));
			int y = zoom * 8 * (TEXT_START_Y + (pos / TEXT_LINE_WIDTH));
			BufferedImage charSymbol = font.get(text.getChar(pos));
			g2d.drawImage(charSymbol.getScaledInstance(charSymbol.getWidth() * zoom, charSymbol.getHeight() * zoom, 0), x, y, null);
		}
	}

	private enum PictureType {
		SMALL, BIG;
	}
}
