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
import ui.BorderSymbols;
import ui.Borders;

public class ClassicRenderer extends JPanel {
	private MonocromeSymbols font;
	private DAXImageContent borders;

	public ClassicRenderer(MonocromeSymbols font, DAXImageContent borders) {
		this.font = font;
		this.borders = borders;

		initRenderer();
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

		BorderSymbols[][] b = Borders.GAME.getSymbols();
		for (int y = 0; y < 24; y++) {
			BorderSymbols[] row = b[y];
			for (int x = 0; x < 40; x++) {
				if (x >= row.length || row[x] == EM) {
					continue;
				}
				BufferedImage s = borders.get(row[x].getIndex());

				g2d.drawImage(s.getScaledInstance(s.getWidth() * 2, s.getHeight() * 2, 0), 16 * x, 16 * y, null);
			}
		}
	}
}
