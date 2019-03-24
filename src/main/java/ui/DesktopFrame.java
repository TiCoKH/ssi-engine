package ui;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import common.FileMap;
import engine.Engine;
import types.UserInterface;
import ui.classic.ClassicMode;
import ui.resource.ResourceViewer;

public class DesktopFrame implements ExceptionHandler {
	private JFrame frame;

	private ResourceViewer resourceUi;

	private UISettings settings;

	private Optional<UserInterface> ui = Optional.empty();

	public DesktopFrame() {
		settings = new UISettings();
		initFrame();
	}

	public void show() {
		this.frame.setVisible(true);
		this.frame.pack();
		this.frame.requestFocus();
	}

	public void startGame(@Nonnull String gameDir, boolean showTitles) {
		ui.ifPresent(uiValue -> {
			uiValue.stop();
			this.frame.remove((JComponent) uiValue);
			this.frame.add(getLogo(), BorderLayout.CENTER);
			this.frame.pack();
		});
		ui = Optional.empty();
		init(gameDir);
		ui.ifPresent(uiValue -> {
			this.frame.remove(getLogo());
			this.frame.add((JComponent) uiValue, BorderLayout.CENTER);
			this.frame.pack();
			uiValue.start(showTitles);
		});
	}

	private void initFrame() {
		this.frame = new JFrame("SSI");
		this.frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.frame.setLocationByPlatform(true);
		this.frame.setJMenuBar(getMainMenu());
		this.frame.add(getLogo(), BorderLayout.CENTER);
	}

	private JMenuBar mainMenu;
	private JMenuItem resource;

	private JMenuBar getMainMenu() {
		if (mainMenu == null) {
			JMenu game = new JMenu("Game");

			JMenuItem openGame = game.add("Open Game");
			openGame.addActionListener(ev -> {
				int result = getChooser().showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
					startGame(getChooser().getSelectedFile().getAbsolutePath(), true);
				}
			});

			JMenu debug = new JMenu("Debug");

			resource = debug.add("Resource Viewer");
			resource.setEnabled(false);
			resource.addActionListener(ev -> resourceUi.show());

			mainMenu = new JMenuBar();
			mainMenu.add(game);
			mainMenu.add(debug);
		}
		return mainMenu;
	}

	private JLabel logo;

	private JLabel getLogo() {
		if (logo == null) {
			ImageIcon logoIcon = new ImageIcon(getClass().getResource("/ssilogo.png"));
			logoIcon.setImage(logoIcon.getImage().getScaledInstance(settings.getZoom() * logoIcon.getIconWidth(),
				settings.getZoom() * logoIcon.getIconHeight(), 0));
			logo = new JLabel(logoIcon);
		}
		return logo;
	}

	private JFileChooser chooser;

	private JFileChooser getChooser() {
		if (chooser == null) {
			chooser = new JFileChooser((File) null);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			chooser.setDialogTitle("Choose game directory");
			chooser.setMultiSelectionEnabled(false);
		}
		return chooser;
	}

	private void init(@Nonnull String dir) {
		try {
			FileMap fm = new FileMap(dir);
			Engine engine = new Engine(fm);
			ui = Optional.of(new ClassicMode(fm, engine, settings, this));
			resourceUi = new ResourceViewer(fm, settings, this);
			resource.setEnabled(true);
		} catch (Exception e) {
			handleException("Error creating game display", e);
		}
	}

	@Override
	public void handleException(@Nonnull String title, @Nonnull Exception e) {
		e.printStackTrace(System.err);
		JOptionPane.showMessageDialog(frame, e.getMessage(), title, JOptionPane.ERROR_MESSAGE);
	}
}
