package ui.classic;

import static data.ContentType.BIGPIC;
import static data.ContentType.BODY;
import static data.ContentType.HEAD;
import static data.ContentType.PIC;
import static data.ContentType.TITLE;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_KP_DOWN;
import static java.awt.event.KeyEvent.VK_KP_LEFT;
import static java.awt.event.KeyEvent.VK_KP_RIGHT;
import static java.awt.event.KeyEvent.VK_KP_UP;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static java.awt.event.KeyEvent.VK_PAGE_UP;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;
import static java.lang.Character.toLowerCase;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.swing.KeyStroke.getKeyStroke;
import static shared.InputAction.LOAD;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import io.vavr.collection.Stream;

import common.FileMap;
import data.ContentType;
import data.dungeon.DungeonMap.VisibleWalls;
import io.vavr.collection.Stream;
import shared.EngineStub;
import shared.GoldboxString;
import shared.GoldboxStringPart;
import shared.InputAction;
import shared.MenuType;
import shared.ProgramMenuType;
import shared.UserInterface;
import shared.ViewDungeonPosition;
import shared.ViewGlobalData;
import shared.ViewOverlandPosition;
import shared.ViewSpacePosition;
import shared.party.CharacterSheet;
import ui.ExceptionHandler;
import ui.UISettings;
import ui.classic.RendererState.DungeonResources;
import ui.shared.Menu;
import ui.shared.UIState;
import ui.shared.resource.DungeonResource;
import ui.shared.resource.ImageCompositeResource;
import ui.shared.resource.ImageResource;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceLoader;
import ui.shared.resource.UIResourceManager;
import ui.shared.text.GoldboxStringInput;
import ui.shared.text.StoryText;

public class ClassicMode extends JPanel implements UserInterface {
	private static final String MENU_PREV = "__MENU_PREV";
	private static final String MENU_NEXT = "__MENU_NEXT";
	private static final String MENU_ACTION = "__MENU_ACTION";

	private static final String INPUT_NUMBER = "INPUT NUMBER: ";
	private static final String INPUT_STRING = "INPUT STRING: ";

	private static final Map<GoldboxString, KeyStroke> KEY_MAPPING;
	static {
		KEY_MAPPING = new HashMap<>();
		KEY_MAPPING.put(InputAction.LOAD, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
		KEY_MAPPING.put(InputAction.SAVE, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		KEY_MAPPING.put(InputAction.FORWARD_UP, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
		KEY_MAPPING.put(InputAction.TURN_LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		KEY_MAPPING.put(InputAction.TURN_RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		KEY_MAPPING.put(InputAction.UTURN_DOWN, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
	}

	private transient EngineStub stub;

	private UIState currentState;
	private Stack<AbstractDialogState> dialogStates = new Stack<>();

	private boolean textNeedsProgressing = false;

	private transient UIResourceConfiguration config;
	private transient UIResourceLoader loader;
	private transient RendererState state;
	private transient UISettings settings;
	private transient RendererContainer renderers;

	private transient ScheduledThreadPoolExecutor exec;
	private transient ScheduledFuture<?> animationFuture;

	private transient ActionMap backupActionMap;
	private transient InputMap backupInputMap;

	private transient boolean running = false;

	private transient Optional<GoldboxStringInput> input = Optional.empty();

	public ClassicMode(@Nonnull FileMap fileMap, @Nonnull EngineStub stub, @Nonnull UIResourceConfiguration config, @Nonnull UISettings settings,
		@Nonnull ExceptionHandler excHandler) throws IOException {

		this.stub = stub;
		this.config = config;
		this.settings = settings;

		this.loader = new UIResourceLoader(fileMap, config);
		UIResourceManager resman = new UIResourceManager(config, loader, settings, excHandler);
		this.state = new RendererState(config, resman);
		this.renderers = new RendererContainer(config, resman, state, settings);

		initSurface();
		resetInput();
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
			showModeMenu();
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
		registerInput("CONTINUE", () -> {
			if (animationFuture != null //
				&& !animationFuture.isDone() //
				&& animationFuture.cancel(false)) {

				exec.execute(titleSwitcher);
			}
		}, getKeyStroke(VK_SPACE, 0), getKeyStroke(VK_ENTER, 0));
		showNextTitle(1);
	}

	private transient Runnable titleSwitcher = null;

	private void showNextTitle(int titleId) {
		state.setPic(createTitleResource(titleId));
		if (titleId < config.getTitleCount() - 1) {
			titleSwitcher = () -> showNextTitle(titleId + 1);
		} else {
			titleSwitcher = () -> showModeMenu();
		}
		animationFuture = exec.schedule(titleSwitcher, 5000, MILLISECONDS);
	}

	public void showModeMenu() {
		state.setPic(createTitleResource(config.getTitleCount()));
		stub.showModeMenu();
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
		state.reset();
		switchUIState(UIState.STORY);
	}

	@Override
	public void clearPictures() {
		stopPicAnimation();
		state.clearPic();
	}

	private void resetInput() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		getActionMap().clear();
		registerInput(LOAD, () -> stub.loadGame(), KEY_MAPPING.get(LOAD));
		registerInput("__PARTY_UP", //
			() -> state.getGlobalData().ifPresent(ViewGlobalData::moveSelectedPartyMemberUp), //
			getKeyStroke(VK_PAGE_UP, 0));
		registerInput("__PARTY_DOWN", //
			() -> state.getGlobalData().ifPresent(ViewGlobalData::moveSelectedPartyMemberDown), //
			getKeyStroke(VK_PAGE_DOWN, 0));
	}

	@Override
	public void setInputNone() {
		resetInput();
	}

	@Override
	public void setInputMenu(@Nonnull MenuType type, @Nonnull List<InputAction> menuItems, @Nullable GoldboxString description,
		@Nullable InputAction selected) {

		resetInput();

		List<InputAction> namedMenuItems = menuItems.stream().filter(a -> a.getName().getLength() > 0).collect(Collectors.toList());
		namedMenuItems.stream().forEach(a -> {
			registerInput(a, () -> {
				state.setMenu(null);
				stub.handleInput(a);
			}, getKeyStroke(toLowerCase(a.getName().toString().charAt(0))));
		});
		if (namedMenuItems.size() > 1) {
			if (type.isHorizontalMenu()) {
				registerInput(MENU_PREV, () -> state.getMenu().ifPresent(Menu::prev), getKeyStroke(VK_LEFT, 0), getKeyStroke(VK_KP_LEFT, 0));
				registerInput(MENU_NEXT, () -> state.getMenu().ifPresent(Menu::next), getKeyStroke(VK_RIGHT, 0), getKeyStroke(VK_KP_RIGHT, 0));
			} else {
				registerInput(MENU_PREV, () -> state.getMenu().ifPresent(Menu::prev), getKeyStroke(VK_UP, 0), getKeyStroke(VK_KP_UP, 0));
				registerInput(MENU_NEXT, () -> state.getMenu().ifPresent(Menu::next), getKeyStroke(VK_DOWN, 0), getKeyStroke(VK_KP_DOWN, 0));
			}
		}

		menuItems.stream().filter(a -> a.getName().getLength() == 0).forEach(a -> {
			registerInput(a, () -> {
				state.setMenu(null);
				stub.handleInput(a);
			}, KEY_MAPPING.get(a.getName()));
		});

		registerInput(MENU_ACTION, () -> {
			state.getMenu().ifPresent(m -> {
				if (m.getType().isPartySelection()) {
					state.getGlobalData().ifPresent(data -> data.setSelectedPartyMember(data.getSelectedPartyMember()));
				}
				state.setMenu(null);
				stub.handleInput(m.getSelectedItem());
			});
		}, getKeyStroke(VK_SPACE, 0), getKeyStroke(VK_ENTER, 0));

		Menu menu = new Menu(type, namedMenuItems, description);
		if (selected != null)
			menu.setSelectedItem(selected);
		state.setMenu(menu);
	}

	@Override
	public void setInputNumber(int maxDigits) {
		backupKeyMaps();
		resetInput();

		input = Optional.of(new GoldboxStringInput(INPUT_NUMBER, maxDigits));

		for (char c = '0'; c <= '9'; c++) {
			mapCharacter(c);
		}

		mapInputBack();

	}

	@Override
	public void setInputString(int maxLetters) {
		backupKeyMaps();
		resetInput();

		input = Optional.of(new GoldboxStringInput(INPUT_STRING, maxLetters));

		for (char c = 'a'; c <= 'z'; c++) {
			mapCharacter(c);
		}
		for (char c = 'A'; c <= 'Z'; c++) {
			mapCharacter(c);
		}
		for (char c = '0'; c <= '9'; c++) {
			mapCharacter(c);
		}
		mapInputBack();
	}

	private void mapCharacter(Character c) {
		registerInput(c, () -> {
			input.ifPresent(input -> {
				input.addChar(Character.toUpperCase(c));
				if (input.getInputCount() == 1) {
					mapInputDone();
				}
			});
		}, getKeyStroke(c));
	}

	private void mapInputBack() {
		registerInput("_INPUT_BACK", () -> {
			input.ifPresent(input -> {
				input.removeLastChar();
				if (input.getInputCount() == 0) {
					getInputMap(WHEN_IN_FOCUSED_WINDOW).remove(getKeyStroke(VK_ENTER, 0));
					getActionMap().remove("_INPUT_DONE");
				}
			});
		}, getKeyStroke(VK_BACK_SPACE, 0));
	}

	private void mapInputDone() {
		registerInput("_INPUT_DONE", () -> {
			input.ifPresent(input -> {
				restoreKeyMaps();
				stub.handleInput(input.toString());
			});
			input = Optional.empty();
		}, getKeyStroke(VK_ENTER, 0));
	}

	private void registerInput(Object actionId, Runnable action, KeyStroke... shortcuts) {
		if (shortcuts != null) {
			for (int i = 0; i < shortcuts.length; i++) {
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(shortcuts[i], actionId);
			}
		}
		getActionMap().put(actionId, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}

	private void backupKeyMaps() {
		if (dialogStates.isEmpty()) {
			backupActionMap = copyCurrentActionMap();
			backupInputMap = copyCurrentInputMap();
		}
	}

	private void restoreKeyMaps() {
		final ActionMap actionMap = !dialogStates.empty() ? dialogStates.peek().getActionMap() : this.backupActionMap;
		getActionMap().clear();
		Stream.of(actionMap.keys()).forEach(key -> getActionMap().put(key, actionMap.get(key)));

		final InputMap inputMap = !dialogStates.empty() ? dialogStates.peek().getInputMap() : this.backupInputMap;
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		Stream.of(inputMap.keys()).forEach(key -> getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, inputMap.get(key)));
	}

	private ActionMap copyCurrentActionMap() {
		final ActionMap result = new ActionMap();
		Stream.of(getActionMap().keys()).forEach(key -> result.put(key, getActionMap().get(key)));
		return result;
	}

	private InputMap copyCurrentInputMap() {
		final InputMap result = new InputMap();
		Stream.of(getInputMap(WHEN_IN_FOCUSED_WINDOW).keys()).forEach(key -> result.put(key, getInputMap(WHEN_IN_FOCUSED_WINDOW).get(key)));
		return result;
	}

	@Override
	public void clearStatus() {
		state.setStatusLine(null);
	}

	@Override
	public void setStatus(@Nonnull GoldboxString status) {
		state.setStatusLine(status);
	}

	@Override
	public void clearCurrentDialog() {
		dialogStates.pop();
		restoreKeyMaps();
	}

	@Override
	public void clearAllDialogs() {
		dialogStates.clear();
		restoreKeyMaps();
	}

	@Override
	public void showProgramMenuDialog(@Nonnull ProgramMenuType programType, @Nonnull List<InputAction> programMenu,
		@Nonnull List<InputAction> horizontalMenu, @Nullable GoldboxString description, @Nonnull InputAction menuSelect) {

		backupKeyMaps();
		resetInput();

		final Menu hMenu = new Menu(MenuType.HORIZONTAL, horizontalMenu);
		final Menu pMenu = new Menu(programType.getMenuType(), programMenu, description);

		horizontalMenu.forEach(a -> {
			registerInput(a, () -> {
				if (menuSelect.equals(a)) {
					stub.handleInput(pMenu.getSelectedItem());
				} else {
					stub.handleInput(a);
				}
			}, getKeyStroke(toLowerCase(a.getName().toString().charAt(0))));
		});
		if (horizontalMenu.size() > 1) {
			registerInput(MENU_PREV, hMenu::prev, getKeyStroke(VK_LEFT, 0), getKeyStroke(VK_KP_LEFT, 0));
			registerInput(MENU_NEXT, hMenu::next, getKeyStroke(VK_RIGHT, 0), getKeyStroke(VK_KP_RIGHT, 0));
		}
		if (programMenu.size() > 1) {
			registerInput(MENU_PREV + "_PROGRAM", pMenu::prev, getKeyStroke(VK_UP, 0), getKeyStroke(VK_KP_UP, 0));
			registerInput(MENU_NEXT + "_PROGRAM", pMenu::next, getKeyStroke(VK_DOWN, 0), getKeyStroke(VK_KP_DOWN, 0));
		}

		registerInput(MENU_ACTION, () -> {
			final ProgramMenuState menuState = (ProgramMenuState) dialogStates.peek();
			final InputAction action = menuState.getHorizontalMenu().getSelectedItem();
			if (menuSelect.equals(action)) {
				stub.handleInput(pMenu.getSelectedItem());
			} else {
				stub.handleInput(action);
			}
		}, getKeyStroke(VK_SPACE, 0), getKeyStroke(VK_ENTER, 0));

		dialogStates
			.push(new ProgramMenuState(state.getGlobalData(), hMenu, copyCurrentActionMap(), copyCurrentInputMap(), menuSelect, pMenu, programType));
	}

	@Override
	public void showCharacterSheet(CharacterSheet sheet, @Nonnull List<InputAction> horizontalMenu) {
		showCharacterSheet(sheet, horizontalMenu, null);
	}

	@Override
	public void showCharacterSheet(CharacterSheet sheet, @Nonnull List<InputAction> horizontalMenu, @Nullable GoldboxString description) {
		backupKeyMaps();
		resetInput();

		final Menu hMenu = new Menu(MenuType.HORIZONTAL, horizontalMenu, description);

		horizontalMenu.forEach(a -> {
			registerInput(a, () -> {
				stub.handleInput(a);
			}, getKeyStroke(toLowerCase(a.getName().toString().charAt(0))));
		});
		if (horizontalMenu.size() > 1) {
			registerInput(MENU_PREV, hMenu::prev, getKeyStroke(VK_LEFT, 0), getKeyStroke(VK_KP_LEFT, 0));
			registerInput(MENU_NEXT, hMenu::next, getKeyStroke(VK_RIGHT, 0), getKeyStroke(VK_KP_RIGHT, 0));
		}

		registerInput(MENU_ACTION, () -> {
			final CharacterSheetState menuState = (CharacterSheetState) dialogStates.peek();
			final InputAction action = menuState.getHorizontalMenu().getSelectedItem();
			stub.handleInput(action);
		}, getKeyStroke(VK_SPACE, 0), getKeyStroke(VK_ENTER, 0));

		dialogStates.push(new CharacterSheetState(sheet, hMenu, copyCurrentActionMap(), copyCurrentInputMap()));
	}

	@Override
	public void setNoResources() {
		switchUIState(UIState.STORY);
		state.clearDungeonResources();
		state.clearOverlandResources();
		state.clearSpaceResources();
	}

	@Override
	public void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nullable VisibleWalls visibleWalls, @Nullable int[][] map,
		int[] decoIds) {

		state.setDungeonResources(position, visibleWalls, map, new DungeonResource(decoIds));
		switchUIState(UIState.DUNGEON);
		state.clearOverlandResources();
		state.clearSpaceResources();
	}

	@Override
	public void setOverlandResources(@Nonnull ViewOverlandPosition position, int mapId) {
		state.setOverlandResources(position, mapId);
		switchUIState(UIState.OVERLAND);
		state.clearDungeonResources();
		state.clearSpaceResources();
	}

	@Override
	public void setSpaceResources(@Nonnull ViewSpacePosition position) {
		state.setSpaceResources(position);
		switchUIState(UIState.SPACE);
		state.clearDungeonResources();
		state.clearOverlandResources();
	}

	@Override
	public void setGlobalData(ViewGlobalData globalData) {
		state.setGlobalData(globalData);
	}

	@Override
	public void showPicture(int headId, int bodyId) {
		stopPicAnimation();
		ImageResource res = new ImageCompositeResource( //
			new ImageResource(headId, HEAD), 0, 0, //
			new ImageResource(bodyId, BODY), 0, 40 //
		);
		state.setPic(res);
	}

	@Override
	public void showPicture(int pictureId, @Nullable ContentType type) {
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
			state.setPic(new ImageResource(pictureId, type));
			updateUIStateForPictureType(type);
		} else {
			state.clearPic();
			state.getDungeonResources().ifPresent(r -> switchUIState(UIState.DUNGEON));
			state.getOverlandResources().ifPresent(r -> switchUIState(UIState.OVERLAND));
			state.getSpaceResources().ifPresent(r -> switchUIState(UIState.SPACE));
		}
		if (state.getPic().isPresent()) {
			startPicAnimation();
		}
	}

	private void updateUIStateForPictureType(@Nonnull ContentType type) {
		if (PIC.equals(type)) {
			switchUIState( //
				state.getDungeonResources().isPresent() ? UIState.DUNGEON : //
					state.getSpaceResources().isPresent() ? UIState.SPACE : //
						UIState.STORY);
		} else if (BIGPIC.equals(type)) {
			switchUIState(UIState.BIGPIC);
		}
	}

	private void startPicAnimation() {
		animationFuture = exec.scheduleWithFixedDelay(() -> state.incPicIndex(), 400, 400, MILLISECONDS);
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
			state.setPic(picture);
		else
			state.clearPic();
		state.getDungeonResources().ifPresent(r -> {
			if (spriteId != 255)
				r.setSprite(spriteId, distance);
			if (state.getPic().isPresent()) {
				spriteReplacement(r);
			}
		});
	}

	@Override
	public void advanceSprite() {
		state.getDungeonResources().ifPresent(r -> {
			r.advanceSprite();
			if (state.getPic().isPresent())
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
		state.getDungeonResources().ifPresent(DungeonResources::clearSprite);
	}

	public void clearText() {
		this.textNeedsProgressing = false;
		state.getStoryText().resetText();
	}

	@Override
	public void addText(boolean withclear, List<GoldboxStringPart> text) {
		StoryText st = state.getStoryText();
		if (withclear)
			st.clearScreen();
		st.addText(text);
		this.textNeedsProgressing = true;
	}

	@Override
	public void addRunicText(GoldboxStringPart text) {
		state.getDungeonResources().ifPresent(r -> r.addRunicText(text));
	}

	private void advance() {
		if (textNeedsProgressing) {
			if (state.getStoryText().hasCharStopReachedLimit()) {
				textNeedsProgressing = false;
				stub.textDisplayFinished();
			} else {
				state.getStoryText().incCharStop(settings.getTextSpeed());
			}
		}
	}

	@Override
	public void switchDungeonAreaMap() {
		state.getDungeonResources().ifPresent(r -> {
			r.toggleShowAreaMap();
		});
	}

	@Override
	public void setPortraitFrameVisible(boolean enabled) {
		renderers.frameRenderer().setPortraitShown(enabled);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		if (!dialogStates.empty()) {
			AbstractDialogState dialogState = dialogStates.peek();
			renderers.rendererFor(dialogState).render((Graphics2D) g, dialogState);
		} else
			renderers.rendererFor(currentState).render((Graphics2D) g);

		input.ifPresent(i -> renderers.inputRenderer().render((Graphics2D) g, i));
	}
}
