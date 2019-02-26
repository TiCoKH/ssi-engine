package main;

import java.awt.EventQueue;

import javax.swing.UIManager;

import ui.DesktopFrame;

public class Goldbox {

	private static final String ARG_NO_TITLE = "--no-title";

	public static void main(String[] args) {
		if (args.length > 2) {
			usageAndExit();
		}
		String dir = args.length > 0 ? args[0] : null;
		if (args.length == 2) {
			if (!args[0].equals(ARG_NO_TITLE) && !args[1].equals(ARG_NO_TITLE)) {
				usageAndExit();
			} else if (args[0].equals(ARG_NO_TITLE)) {
				dir = args[1];
			}
		}

		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		String gameDir = dir;
		EventQueue.invokeLater(() -> {
			DesktopFrame ui = new DesktopFrame();
			ui.show();

			if (gameDir != null) {
				ui.startGame(gameDir, args.length != 2);
			}
		});
	}

	private static void usageAndExit() {
		System.err.println("Run with java -jar <engine jar> [<directory> [--no-title]]");
		System.exit(1);
	}
}
