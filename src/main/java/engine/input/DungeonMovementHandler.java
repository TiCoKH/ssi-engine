package engine.input;

import data.content.DungeonMap;
import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMachine;
import engine.VirtualMemory;
import shared.InputAction;

public class DungeonMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			engine.clear();

			VirtualMachine vm = engine.getVirtualMachine();
			VirtualMemory mem = engine.getMemory();
			DungeonMap map = engine.getDungeonMap();

			if (InputAction.FORWARD_UP == action.getName()) {
				mem.setTriedToLeaveMap(map.couldExit(mem.getDungeonX(), mem.getDungeonY(), mem.getDungeonDir()));
				mem.setMovementBlock(0);
				vm.startMove();
				if (engine.isAbortCurrentThread()) {
					return;
				}
				engine.updatePosition();
				if (mem.getMovementBlock() < 255) {
					if (map.canOpenDoor(mem.getDungeonX(), mem.getDungeonY(), mem.getDungeonDir())) {
						// TODO let the party open the door
						map.openDoor(mem.getDungeonX(), mem.getDungeonY(), mem.getDungeonDir());
					}
					if (map.canMove(mem.getDungeonX(), mem.getDungeonY(), mem.getDungeonDir())) {
						mem.setLastDungeonX(mem.getDungeonX());
						mem.setLastDungeonY(mem.getDungeonY());
						mem.setDungeonX(mem.getDungeonX() + mem.getDungeonDir().getDeltaX());
						mem.setDungeonY(mem.getDungeonY() + mem.getDungeonDir().getDeltaY());
						engine.updatePosition();
					}
				}
				vm.startSearchLocation();
				if (engine.isAbortCurrentThread()) {
					return;
				}
			} else if (InputAction.UTURN_DOWN == action.getName()) {
				mem.setDungeonDir(mem.getDungeonDir().getReverse());
			} else if (InputAction.TURN_LEFT == action.getName()) {
				mem.setDungeonDir(mem.getDungeonDir().getLeft());
			} else if (InputAction.TURN_RIGHT == action.getName()) {
				mem.setDungeonDir(mem.getDungeonDir().getRight());
			}
			engine.updatePosition();
			engine.clearSprite();
			engine.setInputStandard(null);
		});
	}
}
