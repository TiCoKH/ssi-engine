package ui.classic;

import static ui.BorderSymbols.EM;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import engine.InputAction;
import engine.RendererCallback;
import engine.opcodes.EclString;
import ui.BorderSymbols;

public class ClassicRenderer extends JPanel {
	private static final Map<InputAction, KeyStroke> KEY_MAPPING;
	static {
		KEY_MAPPING = new HashMap<>();
		KEY_MAPPING.put(InputAction.ACCEPT, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
		KEY_MAPPING.put(InputAction.MOVE_FORWARD, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0));
		KEY_MAPPING.put(InputAction.TURN_LEFT, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
		KEY_MAPPING.put(InputAction.TURN_RIGHT, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0));
		KEY_MAPPING.put(InputAction.TURN_AROUND, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
	}

	private static final int TEXT_START_X = 1;
	private static final int TEXT_START_Y = 17;
	private static final int TEXT_LINE_WIDTH = 38;
	// Order is FORWARD(FAR,MEDIUM,CLOSE), LEFT(FAR,MEDIUM,CLOSE), RIGHT(FAR,MEDIUM,CLOSE)
	private static final int[] WALL_START_X = { 8, 7, 5, 6, 5, 3, 9, 10, 12 };
	private static final int[] WALL_START_Y = { 7, 6, 4, 6, 4, 3, 6, 4, 3 };

	private RendererCallback renderCB;

	private List<BufferedImage> bwFont;
	private List<BufferedImage> invertedFont;
	private List<BufferedImage> greenFont;
	private List<BufferedImage> magentaFont;
	private DAXImageContent borderSymbols;

	private int zoom;

	private BufferedImage title;

	private EclString statusLine;

	private ClassicBorders layout;
	private PictureType picType;
	private DAXImageContent pic;
	private int picIndex;

	private List<Byte> charList;
	private int textPos;
	private boolean textNeedsProgressing;

	private List<BufferedImage> backdrops;
	private List<BufferedImage> wallSymbols;

	private List<InputAction> menu;

	public ClassicRenderer(RendererCallback renderCB, MonocromeSymbols font, DAXImageContent borderSymbols) {
		this.renderCB = renderCB;
		this.bwFont = font.toList();
		this.invertedFont = font.withInvertedColors();
		this.greenFont = font.withGreenFG();
		this.magentaFont = font.withMagentaFG();
		this.borderSymbols = borderSymbols;

		this.zoom = 4;

		this.title = null;

		this.layout = null;
		this.picType = null;
		this.pic = null;
		this.picIndex = -1;

		this.charList = null;
		this.textPos = 0;
		this.textNeedsProgressing = false;

		this.backdrops = null;
		this.wallSymbols = null;

		this.menu = new ArrayList<>();

		initRenderer();
	}

	public void setInputActions(String description, List<InputAction> newActions) {
		this.menu.clear();
		this.statusLine = description == null ? null : new EclString(description);

		resetInput();

		if (newActions == null) {
			return;
		}

		if (newActions != InputAction.STANDARD_ACTIONS && newActions != InputAction.RETURN_ACTIONS) {
			this.menu.addAll(newActions);
		}
		newActions.stream().forEach(a -> {
			KeyStroke k = KEY_MAPPING.containsKey(a) ? KEY_MAPPING.get(a) : KeyStroke.getKeyStroke(a.getName().toLowerCase().charAt(0));
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(k, a.getName());
			getActionMap().put(a.getName(), new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					renderCB.handleInput(a);
				}
			});
		});
	}

	public void setStatusLine(EclString statusLine) {
		this.statusLine = statusLine;
	}

	public void setTitleScreen(BufferedImage title) {
		this.layout = null;
		this.title = title;
	}

	public void setDungeonDisplay(List<BufferedImage> backdrops, List<BufferedImage> wallSymbols) {
		this.layout = ClassicBorders.GAME;
		this.backdrops = backdrops;
		this.wallSymbols = wallSymbols;
	}

	public void setNoPicture(ClassicBorders b) {
		this.layout = b;
		this.picType = null;
	}

	public void setBigPicture(DAXImageContent pic, int picIndex) {
		this.layout = ClassicBorders.BIGPIC;
		this.picType = PictureType.BIG;
		this.pic = pic;
		this.picIndex = picIndex;
	}

	public void setSmallPicture(DAXImageContent pic, int picIndex) {
		this.layout = ClassicBorders.GAME;
		this.picType = PictureType.SMALL;
		this.pic = pic;
		this.picIndex = picIndex;
	}

	public void clearText() {
		this.textNeedsProgressing = false;
		this.charList = null;
		this.textPos = 0;
	}

	public void addText(EclString text) {
		List<Byte> oldCharList = charList;
		List<Byte> newCharList = new ArrayList<>();
		if (oldCharList != null) {
			newCharList.addAll(oldCharList);
		}
		int wordStart = 0;
		int charCount = newCharList.size() % 38;
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
		this.charList = newCharList;
		this.textNeedsProgressing = true;
	}

	public void addLineBreak() {
		List<Byte> oldCharList = charList;
		if (oldCharList == null) {
			return;
		}
		int charCount = oldCharList.size() % 38;
		List<Byte> newCharList = new ArrayList<>(oldCharList);
		for (int i = charCount; i < 38; i++) {
			newCharList.add((byte) 0x20);
		}
		this.charList = newCharList;
		this.textNeedsProgressing = true;
	}

	public void increaseText() {
		if (textNeedsProgressing) {
			if (textPos == charList.size()) {
				textNeedsProgressing = false;
				renderCB.textDisplayFinished();
			} else {
				textPos++;
			}
		}
	}

	private void initRenderer() {
		setDoubleBuffered(true);
		setPreferredSize(new Dimension(320 * zoom, 200 * zoom));
		resetInput();
	}

	private void resetInput() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "quit");
		getActionMap().clear();
		getActionMap().put("quit", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				renderCB.quit();
				System.exit(0);
			}
		});
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponents(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.BLACK);
		g2d.clearRect(0, 0, 320 * zoom, 200 * zoom);

		if (statusLine != null || !menu.isEmpty()) {
			renderStatus(g2d);
		}
		if (title != null) {
			renderTitle(g2d);
			return;
		}
		if (layout != null) {
			renderBorders(g2d);
		}
		if (picType != null && pic.size() > picIndex) {
			renderPicture(g2d);
		} else if (backdrops != null && wallSymbols != null) {
			renderBackdrop(g2d);
			renderDungeon(g2d);
			renderPosition(g2d);
		}
		renderText(g2d);
	}

	private void renderStatus(Graphics2D g2d) {
		int pos = 0;
		EclString status = statusLine;
		if (status != null) {
			for (; pos < status.getLength(); pos++) {
				renderChar(g2d, pos, 24, status.getChar(pos),
					getActionMap().get(InputAction.ACCEPT.getName()) != null ? invertedFont : !menu.isEmpty() ? magentaFont : greenFont);
			}
			pos++;
		}
		for (InputAction a : menu) {
			EclString menuName = new EclString(a.getName());
			for (int pos2 = 0; pos2 < menuName.getLength(); pos2++) {
				renderChar(g2d, pos + pos2, 24, menuName.getChar(pos2), pos2 == 0 ? bwFont : greenFont);
			}
			pos += menuName.getLength() + 1;
		}
	}

	private void renderTitle(Graphics2D g2d) {
		int x = zoom((320 - title.getWidth()) / 2);
		int y = zoom((200 - title.getHeight()) / 2);
		g2d.drawImage(title.getScaledInstance(zoom(title.getWidth()), zoom(title.getHeight()), 0), x, y, null);
	}

	private void renderBorders(Graphics2D g2d) {
		for (int y = 0; y < 24; y++) {
			BorderSymbols[] row = layout.getSymbols()[y];
			for (int x = 0; x < 40; x++) {
				if (x >= row.length || row[x] == EM) {
					continue;
				}
				BufferedImage s = borderSymbols.get(row[x].getIndex());
				g2d.drawImage(s.getScaledInstance(zoom(s.getWidth()), zoom(s.getHeight()), 0), zoom8(x), zoom8(y), null);
			}
		}
	}

	private void renderPicture(Graphics2D g2d) {
		int x = zoom8(picType == PictureType.SMALL ? 3 : 1);
		BufferedImage p = pic.get(picIndex);
		g2d.drawImage(p.getScaledInstance(zoom(p.getWidth()), zoom(p.getHeight()), 0), x, x, null);
	}

	private void renderText(Graphics2D g2d) {
		List<Byte> chars = charList;
		if (chars == null) {
			return;
		}
		for (int pos = 0; pos < textPos; pos++) {
			int x = TEXT_START_X + (pos % TEXT_LINE_WIDTH);
			int y = TEXT_START_Y + (pos / TEXT_LINE_WIDTH);
			renderChar(g2d, x, y, chars.get(pos), greenFont);
		}
	}

	private void renderBackdrop(Graphics2D g2d) {
		int x = zoom8(3);
		BufferedImage bd = backdrops.get(renderCB.getBackdropIndex());
		g2d.drawImage(bd.getScaledInstance(zoom(bd.getWidth()), zoom(bd.getHeight()), 0), x, x, null);
	}

	private void renderDungeon(Graphics2D g2d) {
		for (WallDistance dis : WallDistance.values()) {
			for (WallPlacement plc : WallPlacement.values()) {
				int[][] wd = renderCB.getWallDisplay(dis, plc);
				if (wd != null) {
					int xStart = WALL_START_X[3 * plc.ordinal() + dis.ordinal()];
					int yStart = WALL_START_Y[3 * plc.ordinal() + dis.ordinal()];
					renderWall(g2d, wd, xStart, yStart);
				}
			}
		}
	}

	private void renderWall(Graphics2D g2d, int[][] wallDisplay, int xStart, int yStart) {
		for (int y = 0; y < wallDisplay.length; y++) {
			int[] row = wallDisplay[y];
			for (int x = 0; x < row.length; x++) {
				int xPos = zoom8(xStart + x);
				int yPos = zoom8(yStart + y);
				BufferedImage w = wallSymbols.get(row[x]);
				g2d.drawImage(w.getScaledInstance(zoom(w.getWidth()), zoom(w.getHeight()), 0), xPos, yPos, null);
			}
		}
	}

	private void renderPosition(Graphics2D g2d) {
		EclString posStr = renderCB.getPositionText();
		for (int pos = 0; pos < posStr.getLength(); pos++) {
			renderChar(g2d, 17 + pos, 15, posStr.getChar(pos), greenFont);
		}
	}

	private void renderChar(Graphics2D g2d, int x, int y, byte c, List<BufferedImage> font) {
		BufferedImage ci = font.get(c);
		g2d.drawImage(ci.getScaledInstance(zoom(ci.getWidth()), zoom(ci.getHeight()), 0), zoom8(x), zoom8(y), null);
	}

	private int zoom(int pos) {
		return zoom * pos;
	}

	private int zoom8(int pos) {
		return zoom * 8 * pos;
	}

	private enum PictureType {
		SMALL, BIG;
	}
}
