package ui;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_Q;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import common.FileMap;
import engine.Engine;
import shared.UserInterface;
import ui.UISettings.ScaleMethod;
import ui.UISettings.TextSpeed;
import ui.classic.ClassicMode;
import ui.debug.EclCodeViewer;
import ui.debug.ResourceViewer;
import ui.shared.resource.UIResourceConfiguration;

public class DesktopFrame implements ExceptionHandler {
	private JFrame frame;

	private ResourceViewer resourceUi;
	private EclCodeViewer codeViewUi;

	private UIResourceConfiguration config;
	private UISettings settings;

	private Optional<UserInterface> ui = Optional.empty();

	public DesktopFrame() {
		settings = new UISettings();
		loadSettings();
		settings.addPropertyChangeListener(e -> {
			ui.ifPresent(UserInterface::resize);
			frame.pack();
		});
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
			this.frame.setTitle("SSI");
			this.frame.add(getLogo(), BorderLayout.CENTER);
			this.frame.pack();
		});
		ui = Optional.empty();
		init(gameDir);
		ui.ifPresent(uiValue -> {
			this.frame.remove(getLogo());
			this.frame.setTitle(config.getGameName());
			this.frame.add((JComponent) uiValue, BorderLayout.CENTER);
			this.frame.pack();
			uiValue.start(showTitles);
		});
	}

	private void initFrame() {
		this.frame = new JFrame("SSI");
		this.frame.setLocationByPlatform(true);
		this.frame.setJMenuBar(getMainMenu());
		this.frame.add(getLogo(), BorderLayout.CENTER);
		this.frame.addWindowListener(new DesktopWindowAdapter());

		JComponent root = this.frame.getRootPane();
		root.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(getKeyStroke(VK_Q, CTRL_DOWN_MASK), "QUIT");
		root.getActionMap().put("QUIT", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
	}

	private JMenuBar mainMenu;
	private JMenu scaleFactor;
	private JMenu scaleMethod;
	private JMenu textSpeed;
	private JMenuItem resource;
	private JMenuItem codeView;

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

			game.addSeparator();

			JMenuItem quit = game.add("Quit");
			quit.setMnemonic(VK_Q);
			quit.addActionListener(ev -> {
				quit();
			});

			scaleFactor = new JMenu("Scale Factor");
			for (int i = 2; i < 6; i++) {
				scaleFactor.add(createScaleFactorItem(i));
			}

			scaleMethod = new JMenu("Scale Method");
			for (ScaleMethod method : ScaleMethod.values()) {
				scaleMethod.add(createScaleMethodItem(method));
			}

			textSpeed = new JMenu("Text Speed");
			for (TextSpeed speed : TextSpeed.values()) {
				textSpeed.add(createTextSpeedItem(speed));
			}

			JMenu options = new JMenu("Options");
			options.add(scaleFactor);
			options.add(scaleMethod);
			options.add(textSpeed);

			JMenu debug = new JMenu("Debug");

			resource = debug.add("Resource Viewer");
			resource.setEnabled(false);
			resource.addActionListener(ev -> resourceUi.show());

			codeView = debug.add("ECL Code");
			codeView.setEnabled(false);
			codeView.addActionListener(ev -> codeViewUi.show());

			mainMenu = new JMenuBar();
			mainMenu.add(game);
			mainMenu.add(options);
			mainMenu.add(debug);
		}
		return mainMenu;
	}

	private JMenuItem createScaleFactorItem(int scale) {
		JMenuItem item = new JRadioButtonMenuItem(scale + "x");
		item.addActionListener(e -> {
			for (int i = 0; i < scaleFactor.getItemCount(); i++)
				((JRadioButtonMenuItem) scaleFactor.getItem(i)).setSelected(false);
			item.setSelected(true);
			settings.setZoom(scale);
		});
		item.setSelected(scale == settings.getZoom());
		return item;

	}

	private JMenuItem createScaleMethodItem(ScaleMethod method) {
		JMenuItem item = new JRadioButtonMenuItem(method.name());
		item.addActionListener(e -> {
			for (int i = 0; i < scaleMethod.getItemCount(); i++)
				((JRadioButtonMenuItem) scaleMethod.getItem(i)).setSelected(false);
			item.setSelected(true);
			settings.setMethod(method);
		});
		item.setSelected(method == settings.getMethod());
		return item;
	}

	private JMenuItem createTextSpeedItem(TextSpeed speed) {
		JMenuItem item = new JRadioButtonMenuItem(speed.name());
		item.addActionListener(e -> {
			for (int i = 0; i < textSpeed.getItemCount(); i++)
				((JRadioButtonMenuItem) textSpeed.getItem(i)).setSelected(false);
			item.setSelected(true);
			settings.setTextSpeed(speed);
		});
		item.setSelected(speed == settings.getTextSpeed());
		return item;
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
			config = new UIResourceConfiguration(fm);
			ui = Optional.of(new ClassicMode(fm, engine, config, settings, this));
			resourceUi = new ResourceViewer(fm, config, settings, this);
			codeViewUi = new EclCodeViewer(fm);
			resource.setEnabled(true);
			codeView.setEnabled(true);
		} catch (Exception e) {
			handleException("Error creating game display", e);
		}
	}

	@Override
	public void handleException(@Nonnull String title, @Nonnull Exception e) {
		e.printStackTrace(System.err);
		JOptionPane.showMessageDialog(frame, e.getMessage(), title, JOptionPane.ERROR_MESSAGE);
	}

	private void quit() {
		ui.ifPresent(UserInterface::stop);
		saveSettings();
		System.exit(0);
	}

	private void loadSettings() {
		File cofigPath = getConfigPath();
		File config = new File(cofigPath, "uisettings.properties");
		if (config.exists() && config.canRead()) {
			try (FileChannel fc = FileChannel.open(config.toPath(), READ)) {
				settings.readFrom(fc);
			} catch (IOException e) {
				handleException("Error reading settings file", e);
			}
		}
	}

	private void saveSettings() {
		File cofigPath = getConfigPath();
		if (!cofigPath.exists()) {
			boolean result = cofigPath.mkdirs();
			if (!result) {
				JOptionPane.showMessageDialog(frame, "Directory couldn't be created.", "Error writing settings file", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		File config = new File(cofigPath, "uisettings.properties");
		try (FileChannel fc = FileChannel.open(config.toPath(), CREATE, WRITE, TRUNCATE_EXISTING);) {
			settings.writeTo(fc);
		} catch (IOException e) {
			handleException("Error writing settings file", e);
		}
	}

	@Nonnull
	private File getConfigPath() {
		File parent = null;
		if (System.getenv("XDG_CONFIG_DIR") != null) {
			parent = new File(System.getenv("XDG_CONFIG_DIR"));
		} else if (System.getProperty("user.home") != null) {
			parent = new File(System.getProperty("user.home"), ".config");
		} else {
			parent = new File(System.getProperty("user.dir"));
		}
		return new File(parent, "ssi-engine");
	}

	private final class DesktopWindowAdapter extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			quit();
		}
	}
}
