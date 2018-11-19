package engine;

import static data.content.DAXContentType.BACK;
import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.ECL;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.DAXContentType.SPRIT;
import static data.content.DAXContentType.WALLDEF;
import static data.content.DAXContentType._8X8D;
import static data.content.WallDef.WallDistance.CLOSE;
import static data.content.WallDef.WallDistance.MEDIUM;
import static data.content.WallDef.WallPlacement.FOWARD;
import static engine.EngineCallback.InputType.STANDARD;
import static engine.EngineCallback.InputType.TITLE;
import static engine.InputAction.MAINMENU_ACTIONS;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import data.content.DAXImageContent;
import data.content.DungeonMap;
import data.content.DungeonMap.Direction;
import data.content.DungeonMap.VisibleWalls;
import data.content.EclProgram;
import data.content.MonocromeSymbols;
import data.content.WallDef;
import engine.opcodes.EclString;
import ui.DungeonResources;
import ui.FontType;
import ui.OverlandResources;
import ui.SpaceResources;
import ui.UICallback;
import ui.UIResources;
import ui.UISettings;
import ui.UIState;
import ui.classic.ClassicMode;

public class Engine implements EngineCallback, UICallback {
	private EngineResources res;
	private ClassicMode ui;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private boolean running = false;
	private boolean abortCurrentThread = false;

	private Thread gameLoop = null;
	private Thread currentThread = null;
	private Thread nextThread = null;

	private Optional<DungeonMap> currentMap = Optional.empty();
	private VisibleWalls visibleWalls = new VisibleWalls();

	private InputAction nextAction = null;

	public Engine(String gameDir) throws IOException {
		this.res = new EngineResources(gameDir);

		MonocromeSymbols font = res.getFont();
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

		UIResources uires = new UIResources(fontMap, res.getBorders().toList());
		UISettings uicfg = new UISettings();
		// TODO load settings

		this.ui = new ClassicMode(this, uires, uicfg);

		this.vm = new VirtualMachine(this);
		this.memory = vm.getMemory();
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
				if (currentThread != null && currentThread.isAlive() && nextThread != null) {
					System.out.println("currentThread still alive, waiting...");
				} else if (nextThread != null) {
					currentThread = nextThread;
					nextThread = null;
					abortCurrentThread = false;
					currentThread.start();
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

	@Override
	public void clear() {
		ui.clear();
	}

	@Override
	public void clearPics() {
		ui.clearPics();
	}

	private void stopCurrentThread() {
		abortCurrentThread = true;
		vm.stopVM();
		continueCurrentThread();
	}

	public void setCurrentThread(Runnable r, String title) {
		stopCurrentThread();
		clear();
		nextThread = new Thread(r, title);
	}

	public void showTitles() {
		ui.setUIState(UIState.TITLE);
		setCurrentThread(() -> {
			synchronized (vm) {
				try {
					for (int i = 1; i < 4; i++) {
						ui.setPic(res.getTitles(i).toList());
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
		ui.setUIState(UIState.TITLE);
		setCurrentThread(() -> {
			try {
				ui.setPic(res.getTitles(4).toList());
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			ui.setInputMenu("BUCK ROGERS V1.2", FontType.GAME_NAME, MAINMENU_ACTIONS);
			pauseCurrentThread();
			if (abortCurrentThread) {
				return;
			}
			clear();
			memory.setGameSpeed(memory.getMenuChoice() == 0 ? 4 : 9);
			loadEcl(memory.getMenuChoice() == 0 ? 16 : 18, false);
		}, "Title Menu");
	}

	@Override
	public void handleInput(InputAction action) {
		this.nextAction = action;
	}

	@Override
	public void setInput(InputType inputType) {
		switch (inputType) {
			case NONE:
				ui.setInputNone();
				break;
			case TITLE:
				ui.setInputTitle();
				break;
			case CONTINUE:
				ui.setInputContinue();
				pauseCurrentThread();
				break;
			case STANDARD:
				ui.setInputStandard();
				break;
		}
	}

	@Override
	public void setInputNumber(int maxDigits) {
		ui.setInputNumber(maxDigits);
		pauseCurrentThread();
	}

	@Override
	public void setInputString(int maxLetters) {
		ui.setInputString(maxLetters);
		pauseCurrentThread();
	}

	@Override
	public void setMenu(List<InputAction> items) {
		ui.setInputMenu(items);
		pauseCurrentThread();
	}

	@Override
	public void loadEcl(int id) {
		loadEcl(id, true);
	}

	public void loadEcl(int id, boolean fromVM) {
		memory.setLastECL(memory.getCurrentECL());
		memory.setCurrentECL(id);
		setCurrentThread(() -> {
			try {
				delayCurrentThread();
				EclProgram ecl = res.find(id, EclProgram.class, ECL);
				vm.newEcl(ecl);
				vm.startInit();
				if (abortCurrentThread) {
					return;
				}
				updatePosition();
				if (fromVM) {
					vm.startMove();
					if (abortCurrentThread) {
						return;
					}
					updatePosition();
					vm.startSearchLocation();
					if (abortCurrentThread) {
						return;
					}
					updatePosition();
					clearPics();
				}
				clearSprite();
				setInput(STANDARD);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}, "VM");
	}

	@Override
	public void loadArea(int id1, int id2, int id3) {
		memory.setAreaValues(id1, id2, id3);
		if (id1 != 127 && id1 != 255) {
			try {
				currentMap = Optional.ofNullable(res.find(id1, DungeonMap.class, GEO));
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		} else {
			currentMap = Optional.empty();
		}
	}

	@Override
	public void loadAreaDecoration(int id1, int id2, int id3) {
		memory.setAreaDecoValues(id1, id2, id3);
		try {
			if (!currentMap.isPresent() && id1 == 1) {
				List<BufferedImage> symbols = res.getSpaceSymbols().toList();

				BufferedImage background = res.getSpaceBackground().get(0);

				ui.setSpaceResources(new SpaceResources(memory, background, symbols));
			} else if (currentMap.isPresent()) {
				WallDef walls = res.find(id1, WallDef.class, WALLDEF);

				List<BufferedImage> wallSymbols = res.findImage(id1, _8X8D).withWallSymbolColor();

				List<BufferedImage> backdrops = new ArrayList<>();
				backdrops.add(res.findImage(128 + id1, BACK).get(0));
				backdrops.add(res.findImage(id1, BACK).get(0));

				ui.setDungeonResources(new DungeonResources(memory, visibleWalls, walls, wallSymbols, backdrops));
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		updateUIState();
	}

	@Override
	public void advanceSprite() {
		ui.advanceSprite();
	}

	@Override
	public void clearSprite() {
		ui.clearSprite();
	}

	@Override
	public void showSprite(int spriteId, int index, int picId) {
		if (currentMap.isPresent()) {
			if (index > 0 && visibleWalls.getVisibleWall(CLOSE, FOWARD)[1] > 0) {
				index = 0;
			}
			if (index > 1 && visibleWalls.getVisibleWall(MEDIUM, FOWARD)[2] > 0) {
				index = 1;
			}
		}
		try {
			DAXImageContent sprite = res.findImage(spriteId, SPRIT);
			DAXImageContent pic = res.findImage(picId, PIC);
			ui.setSprite(sprite.withSpriteColor(), pic != null ? pic.toList() : null, index);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showPicture(int id) {
		if (id == 255 || id == -1) {
			updatePosition();
			clearPics();
			updateUIState();
			return;
		}
		try {
			DAXImageContent smallPic = res.findImage(id, PIC);
			if (smallPic != null) {
				ui.setPic(smallPic.toList());
				updateUIState();
				return;
			}
			DAXImageContent bigPic = res.findImage(id, BIGPIC);
			if (bigPic != null) {
				ui.setPic(bigPic.toList());
				ui.setUIState(UIState.BIGPIC);
				return;
			}
			ui.setPic(null);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showPicture(int gameState, int id) {
		switch (gameState) {
			case 0:
			case 2:
				updateUIState();
				showPicture(id);
				break;
			case 1:
				ui.setUIState(UIState.BIGPIC);
				showPicture(id);
				break;
			case 3:
				showPicture(id);
				break;
			case 4:
				try {
					DAXImageContent map = res.findImage(id, BIGPIC);
					DAXImageContent cursor = res.getOverlandCursor();
					ui.setOverlandResources(new OverlandResources(memory, map.get(0), cursor.withWallSymbolColor().get(0)));
					ui.setUIState(UIState.OVERLAND);
					memory.setAreaDecoValues(255, 127, 127);
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
				break;
			default:
				System.err.println("Unknown game state: " + gameState);
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
	public void updatePosition() {
		currentMap.ifPresent(m -> {
			int x = memory.getDungeonX();
			int y = memory.getDungeonY();
			Direction d = memory.getDungeonDir();
			memory.setWallType(m.wallIndexAt(x, y, d));
			memory.setSquareInfo(m.squareInfoAt(x, y));

			m.visibleWallsAt(visibleWalls, x, y, d);
		});
	}

	public void updateUIState() {
		ui.setUIState(currentMap.map(m -> UIState.DUNGEON).orElse(memory.getAreaDecoValue(0) == 1 ? UIState.SPACE : UIState.STORY));
	}

	@Override
	public void delayCurrentThread() {
		try {
			Thread.sleep(memory.getGameSpeed() * 100L);
		} catch (InterruptedException e) {
		}
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

	public DungeonMap getDungeonMap() {
		return currentMap.get();
	}

	public boolean isAbortCurrentThread() {
		return abortCurrentThread;
	}
}
