package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.ECL;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;
import static engine.EngineCallback.InputType.STANDARD;
import static engine.EngineCallback.InputType.TITLE;
import static engine.InputAction.MAINMENU_ACTIONS;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.content.DAXImageContent;
import data.content.DungeonMap;
import data.content.DungeonMap.Direction;
import data.content.EclProgram;
import data.content.WallDef;
import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import engine.opcodes.EclString;
import ui.classic.ClassicBorders;
import ui.classic.ClassicRenderer;

public class Engine implements EngineCallback, RendererCallback {
	private EngineResources res;
	private ClassicRenderer renderer;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private boolean running;

	private Thread gameLoop;
	private Thread currentThread;

	private WallDef currentWalls;
	private DungeonMap currentMap;

	private InputAction nextAction;

	public Engine(String gameDir) throws IOException {
		res = new EngineResources(gameDir);

		renderer = new ClassicRenderer(this, res.getFont(), res.getBorders());
		renderer.setNoPicture(ClassicBorders.SCREEN);

		vm = new VirtualMachine(this);
		memory = vm.getMemory();

		currentWalls = null;
		currentMap = null;

		running = true;

		nextAction = null;
	}

	public void start() {
		running = true;

		gameLoop = new Thread(() -> {
			while (running) {
				long start = System.currentTimeMillis();
				if (nextAction != null) {
					nextAction.getHandler().handle(this, nextAction);
					nextAction = null;
				}

				renderer.increaseText();
				renderer.repaint();

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
	}

	public void stop() {
		running = false;
		vm.stopVM();
	}

	public void setCurrentThread(Runnable r, String title) {
		currentThread = new Thread(r, title);
		currentThread.start();
	}

	public void showTitles() {
		setCurrentThread(() -> {
			setInput(TITLE);
			synchronized (vm) {
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
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (InterruptedException e) {
				}
				showStartMenu();
			}
		}, "Titles");
	}

	public void showStartMenu() {
		setCurrentThread(() -> {
			try {
				BufferedImage title4 = res.getTitles(4).get(0);
				renderer.setTitleScreen(title4);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			renderer.setInputMenu("BUCK ROGERS V1.2", MAINMENU_ACTIONS);
			pauseCurrentThread();
			renderer.setTitleScreen(null);
			renderer.setStatusLine(null);
			loadEcl(memory.getMenuChoice() == 0 ? 16 : 18);
		}, "Title Menu");
	}

	@Override
	public void handleInput(InputAction action) {
		this.nextAction = action;
	}

	@Override
	public void setInput(InputType inputType) {
		renderer.setStatusLine(null);
		switch (inputType) {
			case NONE:
				renderer.setInputNone();
				break;
			case TITLE:
				renderer.setInputContinue();
				break;
			case CONTINUE:
				renderer.setStatusLine("PRESS BUTTON OR RETURN TO CONTINUE");
				renderer.setInputContinue();
				pauseCurrentThread();
				break;
			case STANDARD:
				renderer.setInputStandard();
				break;
		}
	}

	@Override
	public void setMenu(List<InputAction> items) {
		renderer.setInputMenu(null, items);
		pauseCurrentThread();
	}

	@Override
	public void loadEcl(int id) {
		memory.setLastECL(memory.getCurrentECL());
		memory.setCurrentECL(id);
		currentThread = new Thread(() -> {
			try {
				EclProgram ecl = res.find(id, EclProgram.class, ECL);
				vm.newEcl(ecl);
				vm.startInitial();
				setInput(STANDARD);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}, "VM");
		currentThread.start();
	}

	@Override
	public void loadArea(int id1, int id2, int id3) {
		memory.setAreaValues(id1, id2, id3);
		try {
			currentMap = res.find(id1, DungeonMap.class, GEO);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void loadAreaDecoration(int id1, int id2, int id3) {
		memory.setAreaDecoValues(id1, id2, id3);
		try {
			currentWalls = res.find(id1, WallDef.class, WALLDEF);

			List<BufferedImage> backdrops = new ArrayList<>();
			backdrops.add(res.findImage(128 + id1, BACK).get(0));
			backdrops.add(res.findImage(id1, BACK).get(0));
			List<BufferedImage> currentWallSymbols = res.findImage(id1, _8X8D).withWallSymbolColor();
			renderer.setDungeonDisplay(backdrops, currentWallSymbols);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showPicture(int id) {
		if (id == 255 || id == -1) {
			renderer.setNoPicture(ClassicBorders.GAME);
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
			renderer.setNoPicture(ClassicBorders.GAME);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void addText(EclString str, boolean clear) {
		synchronized (vm) {
			if (clear) {
				renderer.clearText();
			}
			renderer.addText(str);
			// pause VM until all text is displayed
			pauseCurrentThread();
		}
	}

	@Override
	public void addNewline() {
		synchronized (vm) {
			renderer.addLineBreak();
			// pause VM until all text is displayed
			pauseCurrentThread();
		}
	}

	@Override
	public void textDisplayFinished() {
		// continue VM after all text is displayed
		continueCurrentThread();
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
	public int getBackdropIndex() {
		return (memory.getSquareInfo() & 0x80) >> 7;
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

	public void pauseCurrentThread() {
		synchronized (vm) {
			try {
				this.vm.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	public void continueCurrentThread() {
		synchronized (vm) {
			this.vm.notify();
		}
	}

	public ClassicRenderer getRenderer() {
		return renderer;
	}

	public EngineResources getRes() {
		return res;
	}

	public VirtualMachine getVirtualMachine() {
		return vm;
	}

	public VirtualMemory getMemory() {
		return memory;
	}

	public DungeonMap getCurrentMap() {
		return currentMap;
	}

	public WallDef getCurrentWalls() {
		return currentWalls;
	}
}
