package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.ECL;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
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
import ui.UICallback;
import ui.classic.ClassicMode;

public class Engine implements EngineCallback, UICallback {
	private EngineResources res;
	private ClassicMode ui;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private boolean running;
	private boolean abortCurrentThread;

	private Thread gameLoop;
	private Thread currentThread;

	private WallDef currentWalls;
	private DungeonMap currentMap;

	private InputAction nextAction;

	public Engine(String gameDir) throws IOException {
		res = new EngineResources(gameDir);

		ui = new ClassicMode(this, res.getFont(), res.getBorders().toList());

		vm = new VirtualMachine(this);
		memory = vm.getMemory();

		currentWalls = null;
		currentMap = null;

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

				ui.advance();
				ui.repaint();

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

	public void stopCurrentThread() {
		abortCurrentThread = true;
		vm.stopVM();
		continueCurrentThread();
	}

	public void setCurrentThread(Runnable r, String title) {
		abortCurrentThread = false;
		currentThread = new Thread(r, title);
		currentThread.start();
	}

	public void showTitles() {
		setCurrentThread(() -> {
			synchronized (vm) {
				try {
					for (int i = 1; i < 4; i++) {
						BufferedImage title = res.getTitles(i).get(0);
						ui.setTitleScreen(title);
						setInput(TITLE);
						vm.wait(5000L);
						if (abortCurrentThread) {
							return;
						}
					}
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
				ui.setTitleScreen(title4);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			ui.setInputMenu("BUCK ROGERS V1.2", MAINMENU_ACTIONS);
			pauseCurrentThread();
			if (!abortCurrentThread) {
				ui.setTitleScreen(null);
				ui.setStatusLine(null);
				loadEcl(memory.getMenuChoice() == 0 ? 16 : 18);
			}
		}, "Title Menu");
	}

	@Override
	public void handleInput(InputAction action) {
		this.nextAction = action;
	}

	@Override
	public void setInput(InputType inputType) {
		ui.setStatusLine(null);
		switch (inputType) {
			case NONE:
				ui.setInputNone();
				break;
			case TITLE:
				ui.setInputContinue();
				break;
			case CONTINUE:
				ui.setStatusLine("PRESS BUTTON OR RETURN TO CONTINUE");
				ui.setInputContinue();
				pauseCurrentThread();
				break;
			case STANDARD:
				ui.setInputStandard();
				break;
		}
	}

	@Override
	public void setMenu(List<InputAction> items) {
		ui.setInputMenu(null, items);
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
				updatePosition();
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
			ui.setDungeonDisplay(backdrops, currentWallSymbols);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void advanceSprite() {
		ui.decreaseSpritIndex();
	}

	@Override
	public void clearSprite() {
		ui.setSprit(null, 0);
		ui.setNoPicture();
	}

	@Override
	public void showSprite(int id, int index) {
		try {
			DAXImageContent sprit = res.findImage(id, SPRIT);
			ui.setSprit(sprit.withSpritColor(), index);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showPicture(int id) {
		if (id == 255 || id == -1) {
			updatePosition();
			ui.setNoPicture();
			return;
		}
		try {
			DAXImageContent smallPic = res.findImage(id, PIC);
			if (smallPic != null) {
				ui.setSmallPicture(smallPic.toList());
				return;
			}
			DAXImageContent bigPic = res.findImage(id, BIGPIC);
			if (bigPic != null) {
				ui.setBigPicture(bigPic.toList());
				return;
			}
			ui.setNoPicture();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void addText(EclString str, boolean clear) {
		synchronized (vm) {
			if (clear) {
				ui.clearText();
			}
			ui.addText(str);
			// pause VM until all text is displayed
			pauseCurrentThread();
		}
	}

	@Override
	public void addNewline() {
		synchronized (vm) {
			ui.addLineBreak();
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

	@Override
	public void updatePosition() {
		if (memory.getIsDungeon()) {
			updatePosition(memory.getCurrentMapX(), memory.getCurrentMapY(), memory.getCurrentMapOrient());
		} else {
			ui.setPositionText(null);
		}
	}

	public void updatePosition(int x, int y, Direction d) {
		memory.setCurrentMapX(x);
		memory.setCurrentMapY(y);
		memory.setCurrentMapOrient(d);
		memory.setWallType(currentMap.wallIndexAt(x, y, d));
		memory.setSquareInfo(currentMap.squareInfoAt(x, y));
		ui.setPositionText(x + "," + y + " " + d.name().charAt(0));
	}

	public boolean canMove(int x, int y, Direction d) {
		vm.startAddress1();
		if (vm.isStopMove()) {
			return false;
		}
		return currentMap.canMove(x, y, d);
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

	public ClassicMode getUi() {
		return ui;
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
}
