package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

	private boolean shouldShowTitles;
	private boolean running;

	private Thread gameLoop;
	private Thread currentThread;

	private WallDef currentWalls;
	private DungeonMap currentMap;

	private Consumer<InputAction> inputHandler;
	private List<InputAction> viableActions;
	private InputAction nextAction;

	public Engine(String gameDir, boolean showTitles) throws IOException {
		res = new EngineResources(gameDir);

		shouldShowTitles = showTitles;

		renderer = new ClassicRenderer(this, res.getFont(), res.getBorders());
		renderer.setNoPicture(Borders.SCREEN);

		vm = new VirtualMachine(this);
		memory = vm.getMem();

		currentWalls = null;
		currentMap = null;

		running = true;

		this.viableActions = new ArrayList<>();
		nextAction = null;

		gameLoop = new Thread(() -> {
			while (running) {
				long start = System.currentTimeMillis();
				if (nextAction != null) {
					inputHandler.accept(nextAction);
					nextAction = null;
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
		currentThread = new Thread(() -> {
			setInputHandler(InputType.MENU, "BUCK ROGERS V1.2", InputAction.MAINMENU_ACTIONS);
			renderer.setTitleScreen(null);
			renderer.setStatusLine(null);
			int eclId = memory.getMenuChoice() == 0 ? 16 : 18;
			try {
				EclProgram ecl = res.load("ECL1.DAX", eclId, EclProgram.class);
				vm.newEcl(ecl);
				vm.startInitial();
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
		currentThread.start();
	}

	private void showTitles() {
		currentThread = new Thread(() -> {
			setInputHandler(InputType.TITLE, null, null); // TODO
			synchronized (vm) {
				try {
					if (shouldShowTitles) {
						BufferedImage title1 = res.getTitles(1).get(0);
						renderer.setTitleScreen(title1);
						vm.wait(5000L);
						BufferedImage title2 = res.getTitles(2).get(0);
						renderer.setTitleScreen(title2);
						vm.wait(5000L);
						BufferedImage title3 = res.getTitles(3).get(0);
						renderer.setTitleScreen(title3);
						vm.wait(5000L);
					}
					BufferedImage title4 = res.getTitles(4).get(0);
					renderer.setTitleScreen(title4);
				} catch (IOException e) {
					e.printStackTrace(System.err);
				} catch (InterruptedException e) {
				}
				initGameMenu();
			}
		}, "Titles");
		currentThread.start();
	}

	@Override
	public void quit() {
		running = false;
	}

	@Override
	public void handleInput(InputAction action) {
		this.nextAction = action;
	}

	public ClassicRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void setInputHandler(InputType inputType, String description, List<InputAction> viable) {
		this.viableActions.clear();
		if (viable != null)
			this.viableActions.addAll(viable);
		renderer.setInputActions(description, viable);
		switch (inputType) {
			case NONE:
				inputHandler = action -> {
				};
				break;
			case TITLE:
				inputHandler = action -> {
					// Titles thread is current one
					continueCurrentThread();
				};
				break;
			case RETURN:
				inputHandler = action -> {
					setInputHandler(InputType.NONE, null, null);
					// VM thread is the current one
					continueCurrentThread();
				};
				pauseCurrentThread();
				break;
			case MENU:
				inputHandler = action -> {
					int index = viableActions.indexOf(action);
					memory.setMenuChoice(index);
					setInputHandler(InputType.NONE, null, null);
					// either main menu or VM is current thread
					continueCurrentThread();
				};
				pauseCurrentThread();
				break;
			case MOVEMENT:
				inputHandler = action -> {
					renderer.clearText();

					Direction d = memory.getCurrentMapOrient();
					int x = memory.getCurrentMapX();
					int y = memory.getCurrentMapY();
					if (InputAction.MOVE_FORWARD == action) {
						if (currentMap.canMove(x, y, d)) {
							x += d.getDeltaX();
							y += d.getDeltaY();
							memory.setCurrentMapX(x);
							memory.setCurrentMapY(y);
							memory.setSquareInfo(currentMap.squareInfoAt(x, y));

							currentThread = new Thread(() -> {
								vm.startSearchLocation();
							}, "VM");
							currentThread.start();
						}
					} else if (InputAction.TURN_AROUND == action) {
						d = d.getReverse();
					} else if (InputAction.TURN_LEFT == action) {
						d = d.getLeft();
					} else if (InputAction.TURN_RIGHT == action) {
						d = d.getRight();
					}
					memory.setCurrentMapOrient(d);
					memory.setWallType(currentMap.wallIndexAt(x, y, d));
				};
				break;

			default:
				break;
		}
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

	private void pauseCurrentThread() {
		synchronized (vm) {
			try {
				this.vm.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	private void continueCurrentThread() {
		synchronized (vm) {
			this.vm.notify();
		}
	}

	private int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
	}
}
