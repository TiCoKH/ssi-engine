package engine;

import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.ECL;
import static data.content.DAXContentType.GEO;
import static data.content.DAXContentType.PIC;
import static data.content.WallDef.WallDistance.CLOSE;
import static data.content.WallDef.WallDistance.MEDIUM;
import static data.content.WallDef.WallPlacement.FOWARD;
import static engine.EngineInputAction.GAME_MENU_ACTIONS;
import static engine.EngineInputAction.MAIN_MENU_HANDLER;
import static engine.EngineInputAction.MOVEMENT_ACTIONS;
import static engine.EngineInputAction.MOVEMENT_HANDLER;
import static java.nio.file.StandardOpenOption.READ;
import static shared.GameFeature.BODY_HEAD;
import static shared.GameFeature.FLEXIBLE_DUNGEON_SIZE;
import static shared.GameFeature.INTERACTIVE_OVERLAND;
import static shared.GameFeature.OVERLAND_DUNGEON;
import static shared.MenuType.HORIZONTAL;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Strings;

import common.FileMap;
import data.ResourceLoader;
import data.content.DungeonMap;
import data.content.DungeonMap.Direction;
import data.content.DungeonMap.VisibleWalls;
import data.content.DungeonMap2;
import data.content.EclProgram;
import engine.input.MovementHandler;
import engine.opcodes.EclInstruction;
import shared.CustomGoldboxString;
import shared.EngineStub;
import shared.GoldboxString;
import shared.InputAction;
import shared.MenuType;
import shared.UserInterface;

public class Engine implements EngineCallback, EngineStub {
	private ResourceLoader res;
	private EngineConfiguration cfg;
	private UserInterface ui;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private ExecutorService exec;

	private boolean abortCurrentThread = false;
	private boolean showGameMenu = true;
	private InputAction currentMenuItem;

	private Optional<DungeonMap> currentMap = Optional.empty();
	private VisibleWalls visibleWalls = new VisibleWalls();

	public Engine(@Nonnull FileMap fm) throws Exception {
		this.res = new ResourceLoader(fm);
		this.cfg = new EngineConfiguration(fm);

		if (cfg.getCodeBase() != 0)
			EclInstruction.configOpCodes(cfg.getOpCodes());

		this.memory = new VirtualMemory(cfg);
		this.vm = new VirtualMachine(this, this.memory, cfg.getCodeBase());
	}

	@Override
	public void registerUI(@Nonnull UserInterface ui) {
		this.ui = ui;
	}

	@Override
	public void deregisterUI(@Nonnull UserInterface ui) {
		this.ui = null;
	}

	@Override
	public void start() {
		if (ui == null) {
			throw new IllegalStateException("Cant start engine without a registered ui.");
		}
		exec = Executors.newFixedThreadPool(1);
	}

	@Override
	public void stop() {
		stopCurrentTask();
		if (exec != null) {
			exec.shutdown();
			exec.shutdownNow();
		}
	}

	@Override
	public void showStartMenu() {
		setNextTask(() -> {
			List<String> menu = Stream.of("GAME", "DEMO", "START", "DEBUG").filter(e -> !Strings.isNullOrEmpty(cfg.getMainMenuEntry(e)))
				.collect(Collectors.toList());
			setMenu(MenuType.HORIZONTAL,
				menu.stream().map(e -> new EngineInputAction(MAIN_MENU_HANDLER, e, menu.indexOf(e))).collect(Collectors.toList()),
				new CustomGoldboxString(cfg.getMainMenuName()));
			if (abortCurrentThread) {
				return;
			}
			clear();
		});
	}

	@Override
	public void textDisplayFinished() {
		// continue VM after all text is displayed
		continueCurrentThread();
	}

	@Override
	public void handleInput(InputAction action) {
		EngineInputAction eAction = (EngineInputAction) action;
		eAction.getHandler().handle(this, eAction);
	}

	@Override
	public void handleInput(String input) {
		memory.setInput(new CustomGoldboxString(input));
		ui.setInputNone();
		continueCurrentThread();
	}

	@Override
	public void clear() {
		ui.clear();
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

	public void setShowGameMenu(boolean showGameMenu) {
		this.showGameMenu = showGameMenu;
	}

	public void setInputStandard(@Nullable InputAction action) {
		if (action != null)
			this.currentMenuItem = action;
		if (showGameMenu) {
			ui.setInputMenu(HORIZONTAL, GAME_MENU_ACTIONS, null, this.currentMenuItem);
		} else {
			ui.setInputMenu(HORIZONTAL, MOVEMENT_ACTIONS, new CustomGoldboxString("(USE W, S, A, D TO MOVE)"), null);
		}
	}

	@Override
	public void setMenu(@Nonnull MenuType type, @Nonnull List<InputAction> menuItems, @Nullable GoldboxString description) {
		updateOverlandCityCursor();
		ui.setInputMenu(type, menuItems, description, null);
		pauseCurrentThread();
	}

	@Override
	public void loadGame() {
		File savesPath = getSavesPath();
		File saveGame = new File(savesPath, "savegame.dat");
		if (!saveGame.exists() && !saveGame.canRead()) {
			System.err.println("Cant load");
			return;
		}

		setNextTask(() -> {
			getUi().clearAll();

			try (FileChannel fc = FileChannel.open(saveGame.toPath(), READ)) {
				memory.loadFrom(fc);
				loadArea(memory.getAreaValue(0), memory.getAreaValue(1), memory.getAreaValue(2));
				loadAreaDecoration(memory.getAreaDecoValue(0), memory.getAreaDecoValue(1), memory.getAreaDecoValue(2));
				loadEcl(memory.getCurrentECL(), false);
				System.out.println("Game loaded");
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
	}

	@Override
	public void loadEcl(int id) {
		loadEcl(id, true);
	}

	public void loadEcl(int id, boolean fromVM) {
		setNextTask(() -> {
			memory.setLastECL(memory.getCurrentECL());
			memory.setCurrentECL(id);
			memory.setTriedToLeaveMap(false);
			memory.setMovementBlock(0);
			if (fromVM)
				memory.clearTemps();
			if (cfg.isUsingFeature(BODY_HEAD))
				memory.setPictureHeadId(255);

			if (fromVM)
				ui.clearAll();
			else
				clear();

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
				}
				clearSprite();
				this.currentMenuItem = null;
				setShowGameMenu(true);
				setInputStandard(null);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
	}

	public void setNextTask(Runnable r) {
		stopCurrentTask();

		exec.submit(() -> {
			abortCurrentThread = false;
			r.run();
		});
	}

	private void stopCurrentTask() {
		abortCurrentThread = true;
		vm.stopVM();
		continueCurrentThread();
	}

	@Override
	public void loadArea(int id1, int id2, int id3) {
		memory.setAreaValues(id1, id2, id3);
		if (id1 != 127 && id1 != 255) {
			try {
				if (cfg.isUsingFeature(FLEXIBLE_DUNGEON_SIZE)) {
					currentMap = Optional.ofNullable(res.find(id1, DungeonMap2.class, GEO));
				} else {
					currentMap = Optional.ofNullable(res.find(id1, DungeonMap.class, GEO));
				}
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
		if (currentMap.isPresent()) {
			DungeonMap map = currentMap.get();
			VisibleWalls vWalls = isOverlandDungeon() ? null : visibleWalls;
			int[][] mapData = isOverlandDungeon() ? map.generateOverlandMap() : map.generateWallMap();
			int[] decoIds = new int[] { id1, id2, id3 };
			if (cfg.isUsingFeature(FLEXIBLE_DUNGEON_SIZE)) {
				decoIds = new int[7];
				System.arraycopy(((DungeonMap2) map).getDecoIds(), 0, decoIds, 0, 6);
				decoIds[6] = id1;
			}
			ui.setDungeonResources(memory, vWalls, mapData, decoIds);
			MOVEMENT_HANDLER.setMode(MovementHandler.Mode.DUNGEON);
		} else if (id1 == 1) {
			ui.setSpaceResources(memory);
			MOVEMENT_HANDLER.setMode(MovementHandler.Mode.SPACE);
		}
		updatePosition();
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
	public int showSprite(int spriteId, int distance, int picId) {
		if (currentMap.isPresent()) {
			if (distance > 0 && visibleWalls.getVisibleWall(CLOSE, FOWARD)[1] > 0) {
				distance = 0;
			}
			if (distance > 1 && visibleWalls.getVisibleWall(MEDIUM, FOWARD)[2] > 0) {
				distance = 1;
			}
		}
		if (cfg.isUsingFeature(BODY_HEAD) && memory.getPictureHeadId() != 255)
			ui.showSprite(spriteId, memory.getPictureHeadId(), picId, distance);
		else
			ui.showSprite(spriteId, picId, distance);
		return distance;
	}

	@Override
	public void showPicture(int id) {
		if (id == 255 || id == -1) {
			updatePosition();
			clearSprite();
			return;
		}
		if (cfg.isUsingFeature(BODY_HEAD) && memory.getPictureHeadId() != 255) {
			ui.showPicture(memory.getPictureHeadId(), id);
		} else if (!currentMap.isPresent() && cfg.getOverlandMapIds().contains(id)) {
			ui.setOverlandResources(memory, id);
			MOVEMENT_HANDLER.setMode(MovementHandler.Mode.OVERLAND);
		} else {
			ui.showPicture(id, null);
		}
	}

	@Override
	public void showPicture(int gameState, int id) {
		switch (gameState) {
			case 0:
			case 2:
				ui.showPicture(id, null);
				break;
			case 1:
				ui.showPicture(id, BIGPIC);
				break;
			case 3:
				ui.showPicture(id, PIC);
				break;
			case 4:
				ui.setOverlandResources(memory, id);
				MOVEMENT_HANDLER.setMode(MovementHandler.Mode.OVERLAND);
				memory.setAreaDecoValues(255, 127, 127);
				break;
			default:
				System.err.println("Unknown game state: " + gameState);
		}
	}

	@Override
	public void addText(GoldboxString str, boolean clear) {
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
	public void updatePosition() {
		currentMap.ifPresent(m -> {
			int x = memory.getDungeonX();
			int y = memory.getDungeonY();
			Direction d = memory.getDungeonDir();
			memory.setWallType(m.wallIndexAt(x, y, d));
			memory.setSquareInfo(m.squareInfoAt(x, y));

			if (isOverlandDungeon()) {
				memory.setDoorFlags(m.doorFlagsAt(x, y, d));
				memory.setOverlandX(x);
				memory.setOverlandY(y);
				memory.setOverlandDir(d);
			}

			m.visibleWallsAt(visibleWalls, x, y, d);
		});
	}

	private boolean isOverlandDungeon() {
		return cfg.isUsingFeature(OVERLAND_DUNGEON) && memory.getDungeonValue() == 0;
	}

	private static final int[] OVERLAND_CITY_X = { //
		0x03, 0x0B, 0x14, 0x0A, 0x1C, 0x13, 0x25, 0x14, //
		0x1D, 0x1E, 0x18, 0x24, 0x1B, 0x1C, 0x02, 0x0B, //
		0x18, 0x1C, 0x1C, 0x20, 0x12, 0x0F, 0x08, 0x0F, //
		0x13, 0x14, 0x18, 0x18, 0x19, 0x1E, 0x24, 0x21, 0x0E //
	};

	private static final int[] OVERLAND_CITY_Y = { //
		0x0E, 0x07, 0x0A, 0x03, 0x09, 0x03, 0x00, 0x01, //
		0x0C, 0x0E, 0x02, 0x04, 0x01, 0x07, 0x0B, 0x0C, //
		0x09, 0x0B, 0x08, 0x08, 0x07, 0x05, 0x05, 0x02, //
		0x01, 0x01, 0x02, 0x01, 0x02, 0x03, 0x01, 0x00, 0x00 //
	};

	public void updateOverlandCityCursor() {
		if (cfg.isUsingFeature(INTERACTIVE_OVERLAND)) {
			int city = memory.getOverlandCity();
			if (city >= 0 && city <= 32) {
				memory.setOverlandX(OVERLAND_CITY_X[city]);
				memory.setOverlandY(OVERLAND_CITY_Y[city]);
			}
		}
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

	public UserInterface getUi() {
		return ui;
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

	public EngineConfiguration getConfig() {
		return cfg;
	}

	public boolean isAbortCurrentThread() {
		return abortCurrentThread;
	}

	@Nonnull
	public File getSavesPath() {
		File parent = null;
		if (System.getenv("XDG_DATA_DIR") != null) {
			parent = new File(System.getenv("XDG_DATA_DIR"));
		} else if (System.getProperty("user.home") != null) {
			parent = new File(System.getProperty("user.home"), ".local" + File.separator + "share");
		} else {
			parent = new File(System.getProperty("user.dir"));
		}
		parent = new File(parent, "ssi-engine");
		return new File(parent, cfg.getGameName());
	}
}
