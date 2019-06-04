package ui.classic;

import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.BODY;
import static data.content.DAXContentType.HEAD;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.TITLE;
import static engine.InputAction.CONTINUE;
import static engine.InputAction.INPUT_HANDLER;
import static engine.InputAction.LOAD;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import data.content.DungeonMap.VisibleWalls;
import engine.InputAction;
import engine.ViewDungeonPosition;
import engine.ViewOverlandPosition;
import engine.ViewSpacePosition;
import types.EngineStub;
import types.GoldboxString;
import types.MenuType;
import types.UserInterface;
import ui.DungeonResource;
import ui.ExceptionHandler;
import ui.GoldboxStringInput;
import ui.ImageCompositeResource;
import ui.ImageResource;
import ui.Menu;
import ui.UIResourceConfiguration;
import ui.UIResourceLoader;
import ui.UIResourceManager;
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

	private transient Map<UIState, AbstractRenderer> renderers = new EnumMap<>(UIState.class);
	private transient FrameRenderer frameRenderer;
	private UIState currentState;

	private boolean textNeedsProgressing = false;

	private transient UIResourceConfiguration config;
	private transient UIResourceLoader loader;
	private transient UIResources resources;
	private transient UISettings settings;

	private transient ScheduledThreadPoolExecutor exec;
	private transient ScheduledFuture<?> animationFuture;

	private transient boolean running = false;

	private transient GoldboxStringInput input = null;

	public ClassicMode(@Nonnull FileMap fileMap, @Nonnull EngineStub stub, @Nonnull UIResourceConfiguration config, @Nonnull UISettings settings,
		@Nonnull ExceptionHandler excHandler) throws IOException {

		this.stub = stub;
		this.config = config;
		this.settings = settings;

		this.loader = new UIResourceLoader(fileMap, config);
		UIResourceManager resman = new UIResourceManager(loader, settings, excHandler);
		this.frameRenderer = new FrameRenderer(config, resman, settings);
		this.resources = new UIResources(config, resman);

		initRenderers();
		initSurface();
		resetInput();
	}

	private void initRenderers() {
		renderers.clear();
		renderers.put(UIState.TITLE, new TitleRenderer(resources, settings, frameRenderer));
		renderers.put(UIState.STORY, new StoryRenderer(resources, settings, frameRenderer));
		renderers.put(UIState.BIGPIC, new BigPicRenderer(resources, settings, frameRenderer));
		renderers.put(UIState.DUNGEON, new DungeonRenderer(resources, settings, frameRenderer));
		renderers.put(UIState.OVERLAND, new OverlandMapRenderer(resources, settings, frameRenderer));
		renderers.put(UIState.SPACE, new SpaceTravelRenderer(resources, settings, frameRenderer));
	}

	private void initSurface() {
		setDoubleBuffered(true);
		resize();
	}

	@Override
	public void resize() {
		setPreferredSize(new Dimension(settings.zoom(320), settings.zoom(200)));
		invalidate();
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
		resources.setPic(createTitleResource(titleId));
		if (titleId < config.getTitleCount() - 1) {
			titleSwitcher = () -> showNextTitle(titleId + 1);
		} else {
			titleSwitcher = () -> showStartMenu();
		}
		animationFuture = exec.schedule(titleSwitcher, 5000, MILLISECONDS);
	}

	public void showStartMenu() {
		resources.setPic(createTitleResource(config.getTitleCount()));
		stub.showStartMenu();
	}

	private ImageResource createTitleResource(int index) {
		String[] indexes = config.getTitle(index).split(",");
		int i = 0, x1 = 0, y1 = 0;

		int id1 = Integer.parseInt(indexes[i++]);
		if (i < indexes.length && indexes[i].startsWith("+")) {
			x1 = Integer.parseInt(indexes[i++].substring(1));
			y1 = Integer.parseInt(indexes[i++].substring(1));
		}
		if (i < indexes.length) {
			int x2 = 0, y2 = 0;

			int id2 = Integer.parseInt(indexes[i++]);
			if (i < indexes.length && indexes[i].startsWith("+")) {
				x2 = Integer.parseInt(indexes[i++].substring(1));
				y2 = Integer.parseInt(indexes[i++].substring(1));
			}
			return new ImageCompositeResource( //
				new ImageResource(id1, TITLE), x1, y1, //
				new ImageResource(id2, TITLE), x2, y2);
		} else {
			return new ImageCompositeResource( //
				new ImageResource(id1, TITLE), x1, y1);
		}
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
	public void clearAll() {
		setInputNone();
		resources.reset();
		switchUIState(UIState.STORY);
	}

	@Override
	public void clearPictures() {
		stopPicAnimation();
		resources.clearPic();
	}

	private void resetInput() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(LOAD), LOAD);
		getActionMap().clear();
		mapToAction(LOAD);
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

		resources.setDungeonResources(position, visibleWalls, new DungeonResource(decoId1, decoId2, decoId3));
	}

	@Override
	public void setOverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
		resources.setOverlandResources(position, mapId);
	}

	@Override
	public void setSpaceResources(@Nonnull ViewSpacePosition position) {
		resources.setSpaceResources(position);
	}

	@Override
	public void showPicture(int headId, int bodyId) {
		stopPicAnimation();
		ImageResource res = new ImageCompositeResource( //
			new ImageResource(headId, HEAD), 0, 0, //
			new ImageResource(bodyId, BODY), 0, 40 //
		);
		resources.setPic(res);
	}

	@Override
	public void showPicture(int pictureId, @Nullable DAXContentType type) {
		stopPicAnimation();
		if (type == null) {
			try {
				if (loader.idsFor(PIC).contains(pictureId)) {
					type = PIC;
				} else if (loader.idsFor(BIGPIC).contains(pictureId)) {
					type = BIGPIC;
				}
			} catch (IOException e) {
			}
		}
		if (type != null) {
			resources.setPic(new ImageResource(pictureId, type));
			updateUIStateForPictureType(type);
		} else {
			resources.clearPic();
		}
		if (resources.getPic().isPresent()) {
			startPicAnimation();
		}
	}

	private void updateUIStateForPictureType(@Nonnull DAXContentType type) {
		if (PIC.equals(type)) {
			switchUIState( //
				resources.getDungeonResources().isPresent() ? UIState.DUNGEON : //
					resources.getSpaceResources().isPresent() ? UIState.SPACE : //
						UIState.STORY);
		} else if (BIGPIC.equals(type)) {
			switchUIState(UIState.BIGPIC);
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
	public void showSprite(int spriteId, int headId, int bodyId, int distance) {
		ImageResource res = new ImageCompositeResource( //
			new ImageResource(headId, HEAD), 0, 0, //
			new ImageResource(bodyId, BODY), 0, 40 //
		);
		showSprite(spriteId, res, distance);
	}

	@Override
	public void showSprite(int spriteId, int pictureId, int distance) {
		ImageResource res = null;
		if (pictureId != 255)
			res = new ImageResource(pictureId, PIC);
		showSprite(spriteId, res, distance);
	}

	private void showSprite(int spriteId, @Nullable ImageResource picture, int distance) {
		clearSprite();
		if (picture != null)
			resources.setPic(picture);
		else
			resources.clearPic();
		resources.getDungeonResources().ifPresent(r -> {
			if (spriteId != 255)
				r.setSprite(spriteId, distance);
			if (resources.getPic().isPresent()) {
				spriteReplacement(r);
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
				r.clearSprite();
				startPicAnimation();
			}, 1000, MILLISECONDS);
		}
	}

	@Override
	public void clearSprite() {
		clearPictures();
		resources.getDungeonResources().ifPresent(DungeonResources::clearSprite);
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

		renderers.get(currentState).render((Graphics2D) g);
	}
}
