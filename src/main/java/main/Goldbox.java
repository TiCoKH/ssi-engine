package main;

import java.awt.EventQueue;
import java.io.IOException;

import engine.Engine;
import ui.DesktopFrame;

public class Goldbox {

	private static final String ARG_NO_SHOW_TITLE = "-noShowTitle";

	public static void main(String[] args) {
		if (args.length != 1 && args.length != 2) {
			usageAndExit();
		}
		String dir = args[0];
		if (args.length == 2) {
			if (!args[0].equals(ARG_NO_SHOW_TITLE) && !args[1].equals(ARG_NO_SHOW_TITLE)) {
				usageAndExit();
			} else if (args[0].equals(ARG_NO_SHOW_TITLE)) {
				dir = args[1];
			}
		}
		try {
			Engine engine = new Engine(dir);
			EventQueue.invokeLater(() -> {
				new DesktopFrame(engine.getUi());
				engine.start();
				if (args.length == 2) {
					engine.showStartMenu();
				} else {
					engine.showTitles();
				}
			});
		} catch (IOException e) {
			System.err.println("Failure to initialize engine. Quitting.");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	private static void usageAndExit() {
		System.err.println("Run with java -jar <engine jar> <directory> [-noShowTitle]");
		System.exit(1);
	}
}
