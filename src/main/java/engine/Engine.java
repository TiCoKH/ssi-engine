package engine;

import static data.content.DAXContentType.ECL;
import static data.content.DAXContentType.GEO;
import static data.content.WallDef.WallDistance.CLOSE;
import static data.content.WallDef.WallDistance.MEDIUM;
import static data.content.WallDef.WallPlacement.FOWARD;
import static types.GameFeature.BODY_HEAD;

import java.io.File;
import java.io.IOException;
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
import data.content.EclProgram;
import engine.opcodes.EclInstruction;
import types.CustomGoldboxString;
import types.EngineStub;
import types.GoldboxString;
import types.MenuType;
import types.UserInterface;
import types.UserInterface.UIState;

public class Engine implements EngineCallback, EngineStub {
	private ResourceLoader res;
	private EngineConfiguration cfg;
	private UserInterface ui;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private ExecutorService exec;

	private boolean abortCurrentThread = false;

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
			List<String> menu = Stream.of("GAME", "DEMO", "START").filter(e -> !Strings.isNullOrEmpty(cfg.getMainMenuEntry(e)))
				.collect(Collectors.toList());
			setMenu(MenuType.HORIZONTAL,
				menu.stream().map(e -> new InputAction(InputAction.MENU_HANDLER, e, menu.indexOf(e))).collect(Collectors.toList()),
				new CustomGoldboxString(cfg.getMainMenuName()));
			if (abortCurrentThread) {
				return;
			}
			clear();
			memory.setGameSpeed(memory.getMenuChoice() == 0 ? 4 : 9);
			String menuItem = cfg.getMainMenuEntry(menu.get(memory.getMenuChoice()));
			try {
				int ecl = Integer.parseInt(menuItem);
				loadEcl(ecl, false);
			} catch (NumberFormatException e) {
				// TODO in FRUA the entry is not a number
			}
		});
	}

	@Override
	public void textDisplayFinished() {
		// continue VM after all text is displayed
		continueCurrentThread();
	}

	@Override
	public void handleInput(InputAction action) {
		action.getHandler().handle(this, action);
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

	@Override
	public void setMenu(@Nonnull MenuType type, @Nonnull List<InputAction> menuItems, @Nullable GoldboxString description) {
		ui.setInputMenu(type, menuItems, description);
		pauseCurrentThread();
	}

	@Override
	public void loadEcl(int id) {
		loadEcl(id, true);
	}

	public void loadEcl(int id, boolean fromVM) {
		memory.setLastECL(memory.getCurrentECL());
		memory.setCurrentECL(id);
		memory.setTriedToLeaveMap(false);
		memory.setMovementBlock(0);
		if (cfg.isUsingFeature(BODY_HEAD))
			memory.setPictureHeadId(255);
		setNextTask(() -> {
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
				ui.setInputStandard();
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
		if (currentMap.isPresent()) {
			ui.setDungeonResources(memory, visibleWalls, id1, id2, id3);
		} else if (id1 == 1) {
			ui.setSpaceResources(memory);
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
			updateUIState();
			return;
		}
		if (cfg.isUsingFeature(BODY_HEAD) && memory.getPictureHeadId() != 255)
			ui.showPicture(memory.getPictureHeadId(), id);
		else
			ui.showPicture(id, null);
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
				ui.switchUIState(UIState.BIGPIC);
				showPicture(id);
				break;
			case 3:
				showPicture(id);
				break;
			case 4:
				ui.setOverlandResources(memory, id);
				ui.switchUIState(UIState.OVERLAND);
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

			m.visibleWallsAt(visibleWalls, x, y, d);
		});
	}

	private void updateUIState() {
		ui.switchUIState(currentMap.map(m -> UIState.DUNGEON).orElse(memory.getAreaDecoValue(0) == 1 ? UIState.SPACE : UIState.STORY));
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
		return new File(parent, "ssi-engine");
	}
}
