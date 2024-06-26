package engine;

import static data.ContentType.BIGPIC;
import static data.ContentType.ECL;
import static data.ContentType.GEO;
import static data.ContentType.PIC;
import static data.dungeon.WallDef.WallDistance.CLOSE;
import static data.dungeon.WallDef.WallDistance.MEDIUM;
import static data.dungeon.WallDef.WallPlacement.FOWARD;
import static engine.EngineInputAction.DIALOG_MENU_ACTIONS;
import static engine.EngineInputAction.GAME_MENU_ACTIONS;
import static engine.EngineInputAction.MENU_HANDLER;
import static engine.EngineInputAction.MODE_MENU_HANDLER;
import static engine.EngineInputAction.MOVEMENT_ACTIONS;
import static engine.EngineInputAction.MOVEMENT_HANDLER;
import static engine.EngineInputAction.SELECT;
import static engine.text.SpecialCharType.SHARP_S;
import static engine.text.SpecialCharType.UMLAUT_A;
import static engine.text.SpecialCharType.UMLAUT_O;
import static engine.text.SpecialCharType.UMLAUT_U;
import static io.vavr.API.Seq;
import static shared.GameFeature.BODY_HEAD;
import static shared.GameFeature.FLEXIBLE_DUNGEON_SIZE;
import static shared.GameFeature.INTERACTIVE_OVERLAND;
import static shared.GameFeature.OVERLAND_DUNGEON;
import static shared.GameFeature.SPECIAL_CHARS_NOT_FROM_FONT;
import static shared.MenuType.HORIZONTAL;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

import common.FileMap;
import data.Resource;
import data.ResourceLoader;
import data.character.AbstractCharacter;
import data.dungeon.DungeonMap;
import data.dungeon.DungeonMap.Direction;
import data.dungeon.DungeonMap.VisibleWalls;
import data.dungeon.DungeonMap2;
import data.script.EclProgram;
import engine.character.CharacterSheetImpl;
import engine.character.PlayerDataFactory;
import engine.input.MovementHandler;
import engine.script.EclInstruction;
import engine.text.GoldboxStringPartFactory;
import shared.CustomGoldboxString;
import shared.EngineStub;
import shared.GoldboxString;
import shared.GoldboxStringPart;
import shared.InputAction;
import shared.MenuType;
import shared.ProgramMenuType;
import shared.UserInterface;
import shared.party.CharacterSheet;

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

	private Resource<? extends DungeonMap> currentMap = Resource.empty();
	private VisibleWalls visibleWalls = new VisibleWalls();

	private GoldboxStringPartFactory stringPartFactory;

	private final PlayerDataFactory playerDataFactory;

	private boolean showRunicText = false;

	public Engine(@Nonnull FileMap fm) throws Exception {
		this.res = new ResourceLoader(fm);
		this.cfg = new EngineConfiguration(fm);

		if (cfg.getCodeBase() != 0)
			EclInstruction.configOpCodes(cfg.getOpCodes());
		AbstractCharacter.configValues(cfg.getCharacterValues());

		this.memory = new VirtualMemory(cfg);
		this.vm = new VirtualMachine(this, this.memory, cfg.getCodeBase());
		if (cfg.isUsingFeature(SPECIAL_CHARS_NOT_FROM_FONT))
			this.stringPartFactory = new GoldboxStringPartFactory(cfg.getSpecialChar(UMLAUT_A),
				cfg.getSpecialChar(UMLAUT_O), cfg.getSpecialChar(UMLAUT_U), cfg.getSpecialChar(SHARP_S));
		else
			this.stringPartFactory = new GoldboxStringPartFactory();

		this.playerDataFactory = new PlayerDataFactory(res, cfg);
	}

	@Override
	public void registerUI(@Nonnull UserInterface ui) {
		this.ui = ui;
		this.ui.setGlobalData(memory);
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
		exec = Executors.newFixedThreadPool(1, r -> new Thread(r, "Engine Executor"));
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
	public void showModeMenu() {
		setNextTask(() -> {
			Seq<String> menu = Array.of("GAME", "DEMO", "START", "DEBUG")
				.filter(e -> !isNullOrEmpty(cfg.getModeMenuEntry(e)));
			setMenu(MenuType.HORIZONTAL, menu.map(e -> new EngineInputAction(MODE_MENU_HANDLER, e, menu.indexOf(e))),
				new CustomGoldboxString(cfg.getModeMenuName()));
			if (abortCurrentThread) {
				return;
			}
			clear();
		});
	}

	private static boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public void showProgramMenu() {
		Seq<InputAction> menu = Seq( //
			EngineInputAction.CREATE_CHAR, //
			EngineInputAction.VIEW_CHAR, //
			EngineInputAction.MODIFY_CHAR, //
			EngineInputAction.ADD_CHAR, //
			EngineInputAction.REMOVE_CHAR, //
			EngineInputAction.SAVE_GAME, //
			EngineInputAction.BEGIN_ADVENTURE //
		);

		ui.showProgramMenuDialog(ProgramMenuType.PROGRAM, menu, DIALOG_MENU_ACTIONS, null, SELECT);
	}

	@Override
	public void textDisplayFinished() {
		// continue VM after all text is displayed
		continueCurrentThread();
	}

	@Override
	public Resource<CharacterSheet> readCharacter(int id) {
		return getPlayerDataFactory().loadCharacter(id)
			.<CharacterSheet>map(ac -> new CharacterSheetImpl(cfg.getFlavor(), ac));
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
	public void setECLMenu(MenuType type, Seq<GoldboxString> menuItems, GoldboxString description) {
		updateOverlandCityCursor();
		Seq<InputAction> actions = Array.ofAll(menuItems)
			.map(s -> new EngineInputAction(MENU_HANDLER, this.stringPartFactory.fromMenu(s), menuItems.indexOf(s)));
		ui.setInputMenu(type, actions, description, null);
		pauseCurrentThread();
	}

	@Override
	public void setMenu(@Nonnull MenuType type, @Nonnull Seq<InputAction> menuItems,
		@Nullable GoldboxString description) {

		ui.setInputMenu(type, Array.ofAll(menuItems), description, null);
		pauseCurrentThread();
	}

	@Override
	public void loadGame() {
		EngineInputAction.LOAD.handle(this, null);
	}

	@Override
	public void loadEcl(int id) {
		memory.setCurrentECL(id);
		loadEcl(true);
	}

	public void loadEcl(boolean fromVM) {
		setNextTask(() -> {
			memory.setTriedToLeaveMap(false);
			memory.setMovementBlock(0);
			if (fromVM)
				memory.clearTemps();
			if (cfg.isUsingFeature(BODY_HEAD))
				memory.setPictureHeadId(255);

			clear();

			delayCurrentThread();
			final int currentECL = memory.getCurrentECL();
			res.find(currentECL, EclProgram.class, ECL) //
				.ifFailure(this::handleException) //
				.ifPresentAndSuccess(ecl -> {
					vm.newEcl(ecl);
					vm.startInit();
					memory.setLastECL(currentECL);
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
				});
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

	private void loadDungeonMap(int id) {
		if (cfg.isUsingFeature(FLEXIBLE_DUNGEON_SIZE)) {
			currentMap = res.find(id, DungeonMap2.class, GEO);
		} else {
			currentMap = res.find(id, DungeonMap.class, GEO);
		}
	}

	public void cleaDungeonMap() {
		currentMap = Resource.empty();
	}

	@Override
	public void loadArea(int id1, int id2, int id3) {
		memory.setAreaValues(id1, id2, id3);
		if (id1 == 0 && id2 == 0 && id3 == 0) {
			loadDungeonMap(0);
		} else if (id1 != 127 && id1 != 255 && (id2 == 2 || id2 == 127 || id2 == 255)) {
			loadDungeonMap(id1);
		} else if (id1 == 255 && (id2 == 16 || id2 == 2)) {
			showRunicText = id2 == 16;
			if (showRunicText)
				ui.setPortraitFrameVisible(false);
		} else {
			cleaDungeonMap();
		}
		if (!currentMap.isPresent()) {
			ui.setNoResources();
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
			ui.setPortraitFrameVisible(true);
			ui.setDungeonResources(memory, vWalls, mapData, decoIds);
			MOVEMENT_HANDLER.setMode(MovementHandler.Mode.DUNGEON);
		} else if (id1 == 1) {
			ui.setSpaceResources(memory);
			MOVEMENT_HANDLER.setMode(MovementHandler.Mode.SPACE);
		}
		updatePosition();
	}

	@Override
	public void addNpc(int id) {
		getPlayerDataFactory().loadCharacter(id) //
			.ifFailure(this::handleException) //
			.ifPresentAndSuccess(npc -> {
				CharacterSheetImpl cs = new CharacterSheetImpl(cfg.getFlavor(), npc);
				memory.addPartyMember(cs);
			});
	}

	@Override
	public void removeNpc(int index) {
		memory.removePartyMember(memory.getPartyMemberAsCharacterSheet(index));
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
			Seq<GoldboxStringPart> text = Array.ofAll(this.stringPartFactory.from(str));
			if (cfg.getEngineAddress(EngineAddress.TEXT_COLOR) != 0) {
				text = text.insert(0, this.stringPartFactory.fromTextColor(memory.getTextColor()));
			}
			ui.addText(clear, text);
			// pause VM until all text is displayed
			pauseCurrentThread();
		}
	}

	@Override
	public void addRunicText(GoldboxString str) {
		ui.addRunicText(this.stringPartFactory.fromRunicText(str));
	}

	@Override
	public void addNewline() {
		if (showRunicText) {
			ui.addRunicText(this.stringPartFactory.createLineBreak());
		} else {
			synchronized (vm) {
				ui.addText(false, Seq(this.stringPartFactory.createLineBreak()));
				// pause VM until all text is displayed
				pauseCurrentThread();
			}
		}
	}

	@Override
	public void updatePosition() {
		currentMap.ifPresentAndSuccess(m -> {
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

	public PlayerDataFactory getPlayerDataFactory() {
		return playerDataFactory;
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

	private void handleException(Throwable t) {
		t.printStackTrace(System.err);
	}
}
