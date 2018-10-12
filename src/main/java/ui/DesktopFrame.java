package ui;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import javax.swing.JFrame;

import ui.classic.ClassicMode;

public class DesktopFrame {

	private JFrame frame;

	public DesktopFrame(ClassicMode renderer) {
		initFrame(renderer);
		show();
	}

	private void initFrame(ClassicMode renderer) {
		this.frame = new JFrame("SSI");
		this.frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.add(renderer);
	}

	private void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}
}
