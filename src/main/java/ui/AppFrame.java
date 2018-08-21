package ui;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.awt.EventQueue;

import javax.swing.JFrame;

import ui.classic.ClassicRenderer;

public class AppFrame {

	private JFrame frame;

	public AppFrame() {
		this.frame = new JFrame("SSI");
		this.frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.add(new ClassicRenderer());
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(AppFrame::new);
	}
}
