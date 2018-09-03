package ui.classic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import javax.swing.JPanel;

import data.DAXFile;
import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.VGAImage;
import ui.Borders;

public class ClassicRenderer extends JPanel {
	private MonocromeSymbols font;
	private DAXImageContent borders;

	public ClassicRenderer() {
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(640, 400));
		try {
			FileChannel c = FileChannel.open(new File("/mnt/daten/SSI/BUCK11_0.EN/8X8D1.DAX").toPath(), StandardOpenOption.READ);
			DAXFile daxFile = DAXFile.createFrom(c);
			font = daxFile.getById(201, MonocromeSymbols.class);
			c = FileChannel.open(new File("/mnt/daten/SSI/BUCK11_0.EN/BORDERS.DAX").toPath(), StandardOpenOption.READ);
			daxFile = DAXFile.createFrom(c);
			borders = daxFile.getById(0, VGAImage.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.BLACK);
		g2d.clearRect(0, 0, 640, 400);

		int[][] b = Borders.SCREEN.getSymbols();
		for (int y = 0; y < 25; y++) {
			int[] row = b[y];
			for (int x = 0; x < 40; x++) {
				if (x >= row.length || row[x] == -1) {
					continue;
				}
				BufferedImage s = borders.get(row[x]);

				g2d.drawImage(s.getScaledInstance(s.getWidth() * 2, s.getHeight() * 2, 0), 16 * x, 16 * y, null);
			}
		}
	}
}
