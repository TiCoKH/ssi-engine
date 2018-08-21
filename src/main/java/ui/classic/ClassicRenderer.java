package ui.classic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import javax.swing.JPanel;

import data.DAXFile;
import data.content.VGAImage;

public class ClassicRenderer extends JPanel {
	DAXFile<VGAImage> titles;

	public ClassicRenderer() {
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(640, 400));
		try {
			FileChannel c = FileChannel.open(new File("/mnt/daten/SSI/BUCK11_0.EN/TITLE.DAX").toPath(),
					StandardOpenOption.READ);
			titles = DAXFile.createFrom(c, VGAImage.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		super.paintComponents(g);

		BufferedImage i = titles.get(3).getObject().get(0);

		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(i.getScaledInstance(i.getWidth()*2, i.getHeight()*2, 0), 0, 0, null);
	}
}
