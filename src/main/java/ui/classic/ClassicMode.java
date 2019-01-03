package ui.classic;

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

import engine.InputAction;
import types.GoldboxString;
import ui.DungeonResources;
import ui.GoldboxStringInput;
import ui.Menu;
import ui.Menu.MenuType;
import ui.OverlandResources;
import ui.SpaceResources;
import ui.UICallback;
import ui.UIResources;
import ui.UISettings;
import ui.UIState;

public class ClassicMode extends JPanel {
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

	private transient UICallback callback;

	private transient Map<UIState, AbstractRenderer> renderers = new EnumMap<>(UIState.class);
	private UIState currentState;

	private boolean textNeedsProgressing = false;

	private transient UIResources resources;
	private transient UISettings settings;

	private transient ScheduledThreadPoolExecutor exec;
	private transient ScheduledFuture<?> animationFuture;

	private transient GoldboxStringInput input = null;

	public ClassicMode(@Nonnull UICallback callback, @Nonnull UIResources resources, @Nonnull UISettings settings) {
		this.callback = callback;
		this.resources = resources;
		this.settings = settings;

		initRenderers();
		initSurface();
		initExecutorService();
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

	private void initExecutorService() {
		exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
		exec.setRemoveOnCancelPolicy(true);
	}

	public void setUIState(@Nonnull UIState state) {
		this.currentState = state;
	}

	public void clear() {
		setInputNone();
		clearPics();
		clearText();
		clearStatus();
	}

	public void clearPics() {
		setPic(null);
		clearSprite();
	}

	private void resetInput() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(LOAD), LOAD);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(QUIT), QUIT);
		getActionMap().clear();
		mapToAction(LOAD);
		mapToAction(QUIT);
	}

	public void setInputNone() {
		resetInput();
	}

	public void setInputContinue() {
		resetInput();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), CONTINUE);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), CONTINUE);
		mapToAction(CONTINUE);
	}

	public void setInputMenu(@Nonnull MenuType type, @Nonnull List<InputAction> menuItems) {
		setInputMenu(type, menuItems, null);
	}

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
					callback.handleInput(m.getSelectedItem());
				});
			}
		});

		resources.setMenu(new Menu(type, menuItems, description));
	}

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
				callback.handleInput(new InputAction(INPUT_HANDLER, input.toString(), -1));
			}
		});
	}

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
				resources.getSpaceResources().ifPresent(r -> {
					resources.setStatusLine(r.getStatusLine());
				});
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
				callback.handleInput(a);
			}
		});
	}

	public void clearStatus() {
		resources.setStatusLine(null);
	}

	public void setStatus(@Nonnull GoldboxString status) {
		resources.setStatusLine(status);
	}

	public void setDungeonResources(@Nonnull DungeonResources dungeonResources) {
		resources.setDungeonResources(dungeonResources);
	}

	public void setOverlandResources(@Nonnull OverlandResources overlandResources) {
		resources.setOverlandResources(overlandResources);
	}

	public void setSpaceResources(@Nonnull SpaceResources spaceResources) {
		resources.setSpaceResources(spaceResources);
	}

	public void setPic(@Nullable List<BufferedImage> pic) {
		stopPicAnimation();
		resources.setPic(pic);
		if (pic != null && pic.size() > 1) {
			startPicAnimation();
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

	public void setSprite(@Nullable List<BufferedImage> sprite, @Nullable List<BufferedImage> pic, int index) {
		stopPicAnimation();
		resources.setPic(pic);
		resources.getDungeonResources().ifPresent(r -> {
			r.setSprite(sprite, index);
			if (sprite != null && pic != null)
				spriteReplacement(r);
		});
	}

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

	public void clearSprite() {
		setSprite(null, null, 0);
	}

	public void clearText() {
		this.textNeedsProgressing = false;
		resources.setCharList(null);
	}

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

	public void advance() {
		if (textNeedsProgressing) {
			if (resources.hasCharStopReachedLimit()) {
				textNeedsProgressing = false;
				callback.textDisplayFinished();
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
