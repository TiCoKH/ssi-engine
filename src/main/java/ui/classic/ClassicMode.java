package ui.classic;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType.TITLE;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;
import static engine.InputAction.CONTINUE;
import static engine.InputAction.INPUT_HANDLER;
import static engine.InputAction.LOAD;
import static engine.InputAction.QUIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import common.FileMap;
import data.content.DAXContentType;
import data.content.DAXImageContent;
import data.content.DungeonMap.VisibleWalls;
import data.content.MonocromeSymbols;
import data.content.WallDef;
import engine.InputAction;
import engine.ViewDungeonPosition;
import engine.ViewOverlandPosition;
import engine.ViewSpacePosition;
import types.EngineStub;
import types.GoldboxString;
import types.MenuType;
import types.UserInterface;
import ui.ExceptionHandler;
import ui.FontType;
import ui.GoldboxStringInput;
import ui.Menu;
import ui.UIResourceLoader;
import ui.UIResources;
import ui.UIResources.DungeonResources;
import ui.UISettings;

public class ClassicMode extends JPanel implements UserInterface {
	private static final String MENU_PREV = "__MENU_PREV";
	private static final String MENU_NEXT = "__MENU_NEXT";
	private static final String MENU_ACTION = "__MENU_ACTION";

	private static final String INPUT_NUMBER = "INPUT NUMBER: ";
	private static final String INPUT_STRING = "INPUT STRING: ";

	private static final Map<InputAction, KeyStroke> KEY_MAPPING;
	static {
		KEY_MAPPING = new HashMap<>();
		KEY_MAPPING.put(InputAction.LOAD, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		KEY_MAPPING.put(InputAction.SAVE, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		KEY_MAPPING.put(InputAction.QUIT, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

		KEY_MAPPING.put(InputAction.MOVE_FORWARD, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
		KEY_MAPPING.put(InputAction.TURN_LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		KEY_MAPPING.put(InputAction.TURN_RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		KEY_MAPPING.put(InputAction.TURN_AROUND, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));

		KEY_MAPPING.put(InputAction.MOVE_OVERLAND_UP, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
		KEY_MAPPING.put(InputAction.MOVE_OVERLAND_LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		KEY_MAPPING.put(InputAction.MOVE_OVERLAND_RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		KEY_MAPPING.put(InputAction.MOVE_OVERLAND_DOWN, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));

		KEY_MAPPING.put(InputAction.MOVE_SPACE_UP, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
		KEY_MAPPING.put(InputAction.MOVE_SPACE_LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		KEY_MAPPING.put(InputAction.MOVE_SPACE_RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		KEY_MAPPING.put(InputAction.MOVE_SPACE_DOWN, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
	}

	private transient EngineStub stub;

	private transient ExceptionHandler excHandler;

	private transient Map<UIState, AbstractRenderer> renderers = new EnumMap<>(UIState.class);
	private UIState currentState;

	private boolean textNeedsProgressing = false;

	private transient UIResourceLoader loader;
	private transient UIResources resources;
	private transient UISettings settings;

	private transient ScheduledThreadPoolExecutor exec;
	private transient ScheduledFuture<?> animationFuture;

	private transient boolean running = false;

	private transient GoldboxStringInput input = null;

	public ClassicMode(@Nonnull FileMap fileMap, @Nonnull EngineStub stub, @Nonnull UISettings settings, @Nonnull ExceptionHandler excHandler)
		throws IOException {

		this.stub = stub;
		this.settings = settings;
		this.excHandler = excHandler;
		this.loader = new UIResourceLoader(fileMap);

		MonocromeSymbols font = loader.getFont();
		Map<FontType, List<BufferedImage>> fontMap = new EnumMap<>(FontType.class);
		fontMap.put(FontType.NORMAL, font.withGreenFG());
		fontMap.put(FontType.INTENSE, font.withInvertedColors());
		fontMap.put(FontType.SHORTCUT, font.toList());
		fontMap.put(FontType.GAME_NAME, font.withMagentaFG());
		fontMap.put(FontType.DAMAGE, fontMap.get(FontType.SHORTCUT));
		fontMap.put(FontType.PC_HEADING, fontMap.get(FontType.NORMAL));
		fontMap.put(FontType.SEL_PC, fontMap.get(FontType.NORMAL));
		fontMap.put(FontType.PC, fontMap.get(FontType.NORMAL));
		fontMap.put(FontType.FUEL, fontMap.get(FontType.GAME_NAME));
		this.resources = new UIResources(fontMap, loader.getBorders().toList());

		initRenderers();
		initSurface();
		resetInput();
	}

	private void initRenderers() {
		renderers.clear();
		renderers.put(UIState.TITLE, new TitleRenderer(resources, settings));
		renderers.put(UIState.STORY, new StoryRenderer(resources, settings));
		renderers.put(UIState.BIGPIC, new BigPicRenderer(resources, settings));
		renderers.put(UIState.DUNGEON, new DungeonRenderer(resources, settings));
		renderers.put(UIState.OVERLAND, new OverlandMapRenderer(resources, settings));
		renderers.put(UIState.SPACE, new SpaceTravelRenderer(resources, settings));
	}

	private void initSurface() {
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(zoom(320), zoom(200)));
	}

	@Override
	public void start(boolean showTitle) {
		running = true;

		exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
		exec.setRemoveOnCancelPolicy(true);

		Thread gameLoop = new Thread(() -> {
			while (running) {
				long start = System.currentTimeMillis();

				advance();
				repaint();

				long end = System.currentTimeMillis();
				if ((end - start) < 16) {
					try {
						Thread.sleep(16 - (end - start));
					} catch (InterruptedException e) {
						System.err.println("Game Loop was interrupted");
					}
				}
			}
		}, "Game Loop");
		gameLoop.start();

		stub.registerUI(this);
		stub.start();

		switchUIState(UIState.TITLE);
		if (showTitle)
			showTitles();
		else
			showStartMenu();
	}

	@Override
	public void stop() {
		running = false;
		exec.shutdown();
		stopPicAnimation();
		exec.shutdownNow();
		stub.stop();
		stub.deregisterUI(this);
	}

	public void showTitles() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), CONTINUE);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), CONTINUE);
		getActionMap().put(CONTINUE, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (animationFuture != null //
					&& !animationFuture.isDone() //
					&& animationFuture.cancel(false)) {

					exec.execute(titleSwitcher);
				}
			}
		});
		showNextTitle(1);
	}

	private transient Runnable titleSwitcher = null;

	private void showNextTitle(int titleId) {
		loadPicture(titleId, TITLE);
		if (titleId < 3) {
			titleSwitcher = () -> showNextTitle(titleId + 1);
		} else {
			titleSwitcher = () -> showStartMenu();
		}
		animationFuture = exec.schedule(titleSwitcher, 5000, MILLISECONDS);
	}

	public void showStartMenu() {
		showPicture(4, DAXContentType.TITLE);
		stub.showStartMenu();
	}

	@Override
	public void switchUIState(@Nonnull UIState state) {
		this.currentState = state;
	}

	@Override
	public void clear() {
		setInputNone();
		clearSprite();
		clearText();
		clearStatus();
	}

	@Override
	public void clearPictures() {
		stopPicAnimation();
		resources.setPic(null);
	}

	private void resetInput() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(LOAD), LOAD);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(QUIT), QUIT);
		getActionMap().clear();
		mapToAction(LOAD);
		mapToAction(QUIT);
	}

	@Override
	public void setInputNone() {
		resetInput();
	}

	@Override
	public void setInputMenu(@Nonnull MenuType type, @Nonnull List<InputAction> menuItems, @Nullable GoldboxString description) {
		resetInput();

		if (menuItems.size() > 1) {
			menuItems.stream().filter(a -> a.getName().isPresent()).forEach(a -> {
				KeyStroke k = KeyStroke.getKeyStroke(Character.toLowerCase(a.getName().get().toString().charAt(0)));
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(k, a);
				mapToAction(a);
			});
			if (type == MenuType.HORIZONTAL) {
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), MENU_PREV);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), MENU_PREV);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), MENU_NEXT);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), MENU_NEXT);
			} else {
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), MENU_PREV);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), MENU_PREV);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), MENU_NEXT);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), MENU_NEXT);
			}
			getActionMap().put(MENU_PREV, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					resources.getMenu().ifPresent(Menu::prev);
				}
			});
			getActionMap().put(MENU_NEXT, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					resources.getMenu().ifPresent(Menu::next);
				}
			});
		}
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), MENU_ACTION);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), MENU_ACTION);
		getActionMap().put(MENU_ACTION, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resources.getMenu().ifPresent(m -> {
					resources.setMenu(null);
					stub.handleInput(m.getSelectedItem());
				});
			}
		});

		resources.setMenu(new Menu(type, menuItems, description));
	}

	@Override
	public void setInputNumber(int maxDigits) {
		resetInput();
		input = new GoldboxStringInput(INPUT_NUMBER, maxDigits);
		resources.setStatusLine(input);
		for (char c = '0'; c <= '9'; c++) {
			Character d = c;
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(c), d);
			getActionMap().put(d, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					input.addChar(d);
					if (input.getInputCount() == 1) {
						mapInputDone();
					}
				}
			});
		}
		mapInputBack();
	}

	@Override
	public void setInputString(int maxLetters) {
		resetInput();
		input = new GoldboxStringInput(INPUT_STRING, maxLetters);
		resources.setStatusLine(input);
		for (char c = 'a'; c <= 'z'; c++) {
			Character d = c;
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(c), d);
			getActionMap().put(d, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					input.addChar(Character.toUpperCase(d));
					if (input.getInputCount() == 1) {
						mapInputDone();
					}
				}
			});
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			Character d = c;
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(c), d);
			getActionMap().put(d, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					input.addChar(Character.toUpperCase(d));
					if (input.getInputCount() == 1) {
						mapInputDone();
					}
				}
			});
		}
		for (char c = '0'; c <= '9'; c++) {
			Character d = c;
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(c), d);
			getActionMap().put(d, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					input.addChar(Character.toUpperCase(d));
					if (input.getInputCount() == 1) {
						mapInputDone();
					}
				}
			});
		}
		mapInputBack();
	}

	private void mapInputBack() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "_INPUT_BACK");
		getActionMap().put("_INPUT_BACK", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				input.removeLastChar();
			}
		});
	}

	private void mapInputDone() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "_INPUT_DONE");
		getActionMap().put("_INPUT_DONE", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearStatus();
				stub.handleInput(new InputAction(INPUT_HANDLER, input.toString(), -1));
			}
		});
	}

	@Override
	public void setInputStandard() {
		resetInput();
		switch (currentState) {
			case DUNGEON:
				InputAction.DUNGEON_MOVEMENT.stream().forEach(a -> {
					getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(a), a);
					mapToAction(a);
				});
				break;
			case OVERLAND:
				InputAction.OVERLAND_MOVEMENT.stream().forEach(a -> {
					getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(a), a);
					mapToAction(a);
				});
				break;
			case SPACE:
				InputAction.SPACE_MOVEMENT.stream().forEach(a -> {
					getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(a), a);
					mapToAction(a);
				});
				resources.getSpaceResources().ifPresent(r -> resources.setStatusLine(r.getStatusLine()));
				break;
			default:
				System.err.println("Unknown input for current ui state: " + currentState);
		}
	}

	private void mapToAction(@Nonnull InputAction a) {
		getActionMap().put(a, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resources.setMenu(null);
				stub.handleInput(a);
			}
		});
	}

	@Override
	public void clearStatus() {
		resources.setStatusLine(null);
	}

	@Override
	public void setStatus(@Nonnull GoldboxString status) {
		resources.setStatusLine(status);
	}

	@Override
	public void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nonnull VisibleWalls visibleWalls, int decoId1, int decoId2,
		int decoId3) {
		try {
			WallDef walls = loader.find(decoId1, WallDef.class, WALLDEF);

			List<BufferedImage> wallSymbols = loader.findImage(decoId1, _8X8D).toList();

			List<BufferedImage> backdrops = new ArrayList<>();
			backdrops.add(loader.findImage(128 + decoId1, BACK).get(0));
			backdrops.add(loader.findImage(decoId1, BACK).get(0));

			resources.setDungeonResources(position, visibleWalls, walls, wallSymbols, backdrops);
		} catch (IOException e) {
			excHandler.handleException("Could not load dungeon decoration with block Id " + decoId1, e);
		}
	}

	@Override
	public void setOverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
		try {
			DAXImageContent map = loader.findImage(mapId, BIGPIC);
			DAXImageContent cursor = loader.getOverlandCursor();
			resources.setOverlandResources(position, map.get(0), cursor.get(0));
		} catch (NullPointerException | IOException e) {
			excHandler.handleException("Could not load overland map with block Id " + mapId, e);
		}
	}

	@Override
	public void setSpaceResources(@Nonnull ViewSpacePosition position) {
		try {
			List<BufferedImage> symbols = loader.getSpaceSymbols().toList();
			BufferedImage background = loader.getSpaceBackground().get(0);
			resources.setSpaceResources(position, background, symbols);
		} catch (IOException e) {
			excHandler.handleException("Could not load space decoration", e);
		}
	}

	@Override
	public void showPicture(int pictureId, @Nullable DAXContentType type) {
		stopPicAnimation();
		loadPicture(pictureId, type);
		if (resources.getPic().isPresent()) {
			startPicAnimation();
		}
	}

	private void loadPicture(int pictureId, @Nullable DAXContentType type) {
		try {
			DAXImageContent images = loader.findImage(pictureId, type);
			if (images != null) {
				resources.setPic(images.toList());
			} else {
				resources.setPic(null);
			}
		} catch (IOException e) {
			excHandler.handleException("Could not load picture with block Id " + pictureId, e);
		}
	}

	private void startPicAnimation() {
		animationFuture = exec.scheduleWithFixedDelay(() -> resources.incPicIndex(), 400, 400, MILLISECONDS);
	}

	private void stopPicAnimation() {
		if (animationFuture != null) {
			animationFuture.cancel(true);
			animationFuture = null;
		}
	}

	@Override
	public void showSprite(int spriteId, int pictureId, int distance) {
		clearSprite();
		loadPicture(pictureId, PIC);
		resources.getDungeonResources().ifPresent(r -> {
			try {
				DAXImageContent sprite = loader.findImage(spriteId, SPRIT);
				if (sprite != null) {
					r.setSprite(sprite.toList(), distance);
					if (resources.getPic().isPresent()) {
						spriteReplacement(r);
					}
				}
			} catch (IOException e) {
				excHandler.handleException("Could not load sprite with block Id " + spriteId, e);
			}
		});
	}

	@Override
	public void advanceSprite() {
		resources.getDungeonResources().ifPresent(r -> {
			r.advanceSprite();
			if (resources.getPic().isPresent())
				spriteReplacement(r);
		});
	}

	private void spriteReplacement(DungeonResources r) {
		if (!r.spriteAdvancementPossible() && animationFuture == null) {
			animationFuture = exec.schedule(() -> {
				r.setSprite(null, 0);
				startPicAnimation();
			}, 1000, MILLISECONDS);
		}
	}

	@Override
	public void clearSprite() {
		clearPictures();
		resources.getDungeonResources().ifPresent(r -> r.setSprite(null, 0));
	}

	@Override
	public void clearText() {
		this.textNeedsProgressing = false;
		resources.setCharList(null);
	}

	@Override
	public void addText(GoldboxString text) {
		List<Byte> newCharList = new ArrayList<>();

		int lineWidth = renderers.get(currentState).getLineWidth();
		int wordStart = 0;
		int charCount = resources.getCharCount() % lineWidth;
		for (int i = 0; i < text.getLength(); i++) {
			boolean endOfText = i + 1 == text.getLength();
			if (text.getChar(i) == ' ' || endOfText) {
				// Space is not part of the word, last char is.
				int wordLength = (i - wordStart) + (endOfText ? 1 : 0);
				if (charCount + wordLength > lineWidth) {
					for (int j = charCount; j < lineWidth; j++) {
						newCharList.add((byte) 0x20);
					}
					charCount = 0;
				}
				for (int j = wordStart; j < wordStart + wordLength; j++) {
					newCharList.add(text.getChar(j));
				}
				wordStart = i + 1;
				charCount += wordLength;
				if (charCount < lineWidth && !endOfText) {
					newCharList.add((byte) 0x20);
					charCount++;
				} else {
					charCount = 0;
				}
			}
		}
		resources.addChars(newCharList);
		this.textNeedsProgressing = true;
	}

	@Override
	public void addLineBreak() {
		int lineWidth = renderers.get(currentState).getLineWidth();
		int charCount = resources.getCharCount() % lineWidth;
		List<Byte> newCharList = new ArrayList<>();
		for (int i = charCount; i < lineWidth; i++) {
			newCharList.add((byte) 0x20);
		}
		resources.addChars(newCharList);
		this.textNeedsProgressing = true;
	}

	private void advance() {
		if (textNeedsProgressing) {
			if (resources.hasCharStopReachedLimit()) {
				textNeedsProgressing = false;
				stub.textDisplayFinished();
			} else {
				resources.incCharStop();
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.BLACK);
		g2d.clearRect(0, 0, zoom(320), zoom(200));

		renderers.get(currentState).render(g2d);
	}

	private int zoom(int pos) {
		return settings.getZoom() * pos;
	}
}
