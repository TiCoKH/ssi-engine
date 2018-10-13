package ui.classic;

import static engine.InputAction.CONTINUE;
import static engine.InputAction.LOAD;
import static engine.InputAction.QUIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ui.FontType.INTENSE;

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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import engine.InputAction;
import engine.opcodes.EclString;
import ui.DungeonResources;
import ui.FontType;
import ui.StatusLine;
import ui.UICallback;
import ui.UIResources;
import ui.UISettings;
import ui.UIState;

public class ClassicMode extends JPanel {
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
	}

	private transient UICallback callback;

	private transient Map<UIState, AbstractRenderer> renderers = new EnumMap<>(UIState.class);
	private UIState currentState;

	private boolean textNeedsProgressing = false;

	private transient UIResources resources;
	private transient UISettings settings;

	private transient ScheduledThreadPoolExecutor exec;
	private transient ScheduledFuture<?> animationFuture;

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
		setPic(null);
		clearText();
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

	public void setInputTitle() {
		resetInput();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), CONTINUE);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), CONTINUE);
		mapToAction(CONTINUE);
	}

	public void setInputContinue() {
		resetInput();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), CONTINUE);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), CONTINUE);
		mapToAction(CONTINUE);
		resources.setStatusLine(StatusLine.of("PRESS BUTTON OR RETURN TO CONTINUE", INTENSE));
	}

	public void setInputMenu(@Nonnull List<InputAction> newActions) {
		setInputMenu(null, null, newActions);
	}

	public void setInputMenu(@Nullable String statusLine, @Nullable FontType textFont, @Nonnull List<InputAction> newActions) {
		resetInput();

		newActions.stream().forEach(a -> {
			KeyStroke k = KeyStroke.getKeyStroke(a.getName().toLowerCase().charAt(0));
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(k, a);
			mapToAction(a);
		});

		List<String> menu = newActions.stream().map(InputAction::getName).collect(Collectors.toList());
		resources.setStatusLine(StatusLine.of(statusLine, textFont, menu));
	}

	public void setInputStandard() {
		resetInput();
		InputAction.STANDARD_ACTIONS.stream().forEach(a -> {
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KEY_MAPPING.get(a), a);
			mapToAction(a);
		});
	}

	private void mapToAction(@Nonnull InputAction a) {
		getActionMap().put(a, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resources.setStatusLine(null);
				callback.handleInput(a);
			}
		});
	}

	public void setDungeonResources(DungeonResources dungeonResources) {
		resources.setDungeonResources(dungeonResources);
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
			if (sprite != null)
				spriteReplacement(r);
		});
	}

	public void advanceSprite() {
		resources.getDungeonResources().ifPresent(r -> {
			r.advanceSprite();
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

	public void addText(EclString text) {
		List<Byte> newCharList = new ArrayList<>();

		int wordStart = 0;
		int charCount = resources.getCharCount() % 38;
		for (int i = 0; i < text.getLength(); i++) {
			boolean endOfText = i + 1 == text.getLength();
			if (text.getChar(i) == ' ' || endOfText) {
				// Space is not part of the word, last char is.
				int wordLength = (i - wordStart) + (endOfText ? 1 : 0);
				if (charCount + wordLength > 38) {
					for (int j = charCount; j < 38; j++) {
						newCharList.add((byte) 0x20);
					}
					charCount = 0;
				}
				for (int j = wordStart; j < wordStart + wordLength; j++) {
					newCharList.add(text.getChar(j));
				}
				wordStart = i + 1;
				charCount += wordLength;
				if (charCount < 38 && !endOfText) {
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
		int charCount = resources.getCharCount() % 38;
		List<Byte> newCharList = new ArrayList<>();
		for (int i = charCount; i < 38; i++) {
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
