package ui;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import javax.swing.JFrame;

import ui.classic.ClassicMode;

public class DesktopFrame {

	private ClassicMode ui;

	private JFrame frame;

	public DesktopFrame(ClassicMode ui) {
		this.ui = ui;
		initFrame();
		show();
	}

	private void initFrame() {
		this.frame = new JFrame("SSI");
		this.frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.add(ui);
	}

	private void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}
}
