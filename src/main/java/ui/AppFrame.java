package ui;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.JFrame;

import engine.Engine;

public class AppFrame {

	private Engine engine;
	private JFrame frame;

	public AppFrame(String dir, boolean showTitles) throws IOException {
		initEngine(dir, showTitles);
		initFrame();
		show();
	}

	private void initEngine(String dir, boolean showTitles) throws IOException {
		engine = new Engine(dir, showTitles);
	}

	private void initFrame() {
		this.frame = new JFrame("SSI");
		this.frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.add(engine.getRenderer());
	}

	private void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}

	public static void main(String[] args) {
		if (args.length != 1 && args.length != 2) {
			System.err.println("Run with java AppFrame <directory> [-noShowTitle]");
			System.exit(1);
		}
		EventQueue.invokeLater(() -> {
			try {
				new AppFrame(args[0], args.length != 2);
			} catch (IOException e) {
				System.err.println("Failure to initialize engine. Quitting.");
				e.printStackTrace(System.err);
				System.exit(1);
			}
		});
	}
}
