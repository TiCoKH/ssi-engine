package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.content.DAXImageContent;
import data.content.DungeonMap;
import data.content.DungeonMap.Direction;
import data.content.EclProgram;
import data.content.WallDef;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import engine.opcodes.EclString;
import ui.Borders;
import ui.classic.ClassicRenderer;

public class Engine implements EngineCallback, RendererCallback {
	private EngineResources res;
	private ClassicRenderer renderer;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private boolean showingTitle;
	private boolean running;

	private Thread gameLoop;
	private Thread currentThread;

	private WallDef currentWalls;
	private DungeonMap currentMap;

	private KeyEvent keyEvent;
	private Map<Integer, Runnable> shortcuts;

	public Engine(String gameDir) throws IOException {
		res = new EngineResources(gameDir);

		renderer = new ClassicRenderer(this, res.getFont(), res.getBorders());
		renderer.setNoPicture(Borders.SCREEN);

		vm = new VirtualMachine(this);
		memory = vm.getMem();

		currentWalls = null;
		currentMap = null;

		keyEvent = null;
		shortcuts = new HashMap<>();

		showingTitle = false;
		running = true;

		gameLoop = new Thread(() -> {
			while (running) {
				long start = System.currentTimeMillis();
				if (keyEvent != null) {
					handleInput();
					keyEvent = null;
				}
				renderer.increaseText();
				renderer.repaint();
				long end = System.currentTimeMillis();
				if ((end - start) < 16) {
					try {
						Thread.sleep(16 - (end - start));
					} catch (InterruptedException e) {
					}
				}
			}
		}, "Game Loop");
		gameLoop.start();

		showTitles();
	}

	private void initGameMenu() {
		shortcuts.clear();
		shortcuts.put(KeyEvent.VK_D, () -> {
			try {
				renderer.setTitleScreen(null);
				renderer.setStatusLine(null);
				EclProgram game = res.load("ECL1.DAX", 18, EclProgram.class);
				vm.newEcl(game);
				vm.startInitial();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
		shortcuts.put(KeyEvent.VK_G, () -> {
			try {
				renderer.setTitleScreen(null);
				renderer.setStatusLine(null);
				EclProgram demo = res.load("ECL1.DAX", 16, EclProgram.class);
				vm.newEcl(demo);
				vm.startInitial();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
	}

	private void showTitles() {
		currentThread = new Thread(() -> {
			synchronized (vm) {
				showingTitle = true;
				try {
					BufferedImage title1 = res.getTitles(1).get(0);
					renderer.setTitleScreen(title1);
					vm.wait(5000L);
					BufferedImage title2 = res.getTitles(2).get(0);
					renderer.setTitleScreen(title2);
					vm.wait(5000L);
					BufferedImage title3 = res.getTitles(3).get(0);
					renderer.setTitleScreen(title3);
					vm.wait(5000L);
					BufferedImage title4 = res.getTitles(4).get(0);
					renderer.setTitleScreen(title4);
					renderer.setStatusLine(new EclString("BUCK ROGERS V1.2 GAME DEMO"));
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (InterruptedException e) {
				}
				showingTitle = false;
				initGameMenu();
			}
		}, "Titles");
		currentThread.start();
	}

	private void handleInput() {
		// System.out.println(typedkey.paramString());

		if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_Q) {
			running = false;
			System.exit(0);
		}

		if (showingTitle) {
			synchronized (vm) {
				vm.notify();
			}
			return;
		}

		if (shortcuts.containsKey(keyEvent.getKeyCode())) {
			currentThread = new Thread(shortcuts.get(keyEvent.getKeyCode()), "Current Activity");
			shortcuts.clear();
			currentThread.start();
			return;
		}

		Direction d = memory.getCurrentMapOrient();
		switch (keyEvent.getKeyCode()) {
			case KeyEvent.VK_W:
				if (currentMap.canMove(memory.getCurrentMapX(), memory.getCurrentMapY(), d)) {
					memory.setCurrentMapX(memory.getCurrentMapX() + d.getDeltaX());
					memory.setCurrentMapY(memory.getCurrentMapY() + d.getDeltaY());
				}
				break;
			case KeyEvent.VK_S:
				if (currentMap.canMove(memory.getCurrentMapX(), memory.getCurrentMapY(), d.getReverse())) {
					memory.setCurrentMapX(memory.getCurrentMapX() + d.getReverse().getDeltaX());
					memory.setCurrentMapY(memory.getCurrentMapY() + d.getReverse().getDeltaY());
				}
				break;
			case KeyEvent.VK_A:
				memory.setCurrentMapOrient(memory.getCurrentMapOrient().getLeft());
				break;
			case KeyEvent.VK_D:
				memory.setCurrentMapOrient(memory.getCurrentMapOrient().getRight());
				break;
		}
	}

	public ClassicRenderer getRenderer() {
		return renderer;
	}

	public void setKeyEvent(KeyEvent e) {
		this.keyEvent = e;
	}

	@Override
	public void loadArea(int id1, int id2, int id3) {
		memory.setCurrentMapX(0);
		memory.setCurrentMapY(0);
		memory.setCurrentMapOrient(Direction.NORTH);

		try {
			currentMap = res.find(id1, DungeonMap.class, GEO);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void loadAreaDecoration(int id1, int id2, int id3) {
		try {
			currentWalls = res.find(id1, WallDef.class, WALLDEF);

			List<BufferedImage> currentBackdrop = res.findImage(id1, BACK).toList();
			List<BufferedImage> currentWallSymbols = res.findImage(id1, _8X8D).withWallSymbolColor();
			renderer.setDungeonDisplay(currentBackdrop, currentWallSymbols);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showPicture(int id) {
		if (id == 255 || id == -1) {
			renderer.setNoPicture(Borders.GAME);
			return;
		}
		try {
			DAXImageContent smallPic = res.findImage(id, PIC);
			if (smallPic != null) {
				renderer.setSmallPicture(smallPic, 0);
				return;
			}
			DAXImageContent bigPic = res.findImage(id, BIGPIC);
			if (bigPic != null) {
				renderer.setBigPicture(bigPic, 0);
				return;
			}
			renderer.setNoPicture(Borders.GAME);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showText(EclString str) {
		synchronized (vm) {
			renderer.setText(str);
			try {
				vm.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void textDisplayFinished() {
		synchronized (vm) {
			vm.notify();
		}
	}

	@Override
	public EclString getPositionText() {
		StringBuilder sb = new StringBuilder();
		sb.append(memory.getCurrentMapX());
		sb.append(",");
		sb.append(memory.getCurrentMapY());
		sb.append(" ");
		sb.append(memory.getCurrentMapOrient().name().charAt(0));
		return new EclString(sb.toString());
	}

	@Override
	public int[][] getWallDisplay(WallDistance dis, WallPlacement plc) {
		Direction d = memory.getCurrentMapOrient();
		int m = dis == WallDistance.FAR ? 2 : dis == WallDistance.MEDIUM ? 1 : 0;

		int xPos = memory.getCurrentMapX() + m * d.getDeltaX();
		if (xPos < 0 || xPos > 15) {
			return null;
		}
		int yPos = memory.getCurrentMapY() + m * d.getDeltaY();
		if (yPos < 0 || yPos > 15) {
			return null;
		}
		int wallIndex;
		switch (plc) {
			case FOWARD:
				wallIndex = currentMap.wallIndexAt(xPos, yPos, d);
				break;
			case LEFT:
				wallIndex = currentMap.wallIndexAt(xPos, yPos, d.getLeft());
				break;
			case RIGHT:
				wallIndex = currentMap.wallIndexAt(xPos, yPos, d.getRight());
				break;
			default:
				throw new IllegalArgumentException("invalid direction " + plc);
		}
		if (wallIndex == 0) {
			return null;
		}
		return currentWalls.getWallDisplay(wallIndex - 1, dis, plc);
	}

	private int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
	}
}
