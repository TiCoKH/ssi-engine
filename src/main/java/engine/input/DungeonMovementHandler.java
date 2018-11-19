package engine.input;

import static engine.EngineCallback.InputType.STANDARD;

import data.content.DungeonMap;
import engine.Engine;
import engine.InputAction;
import engine.VirtualMachine;
import engine.VirtualMemory;

public class DungeonMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.setCurrentThread(() -> {
			VirtualMachine vm = engine.getVirtualMachine();
			VirtualMemory mem = engine.getMemory();
			DungeonMap map = engine.getDungeonMap();

			if (InputAction.MOVE_FORWARD == action) {
				mem.setTriedToLeaveMap(map.couldExit(mem.getDungeonX(), mem.getDungeonY(), mem.getDungeonDir()));
				mem.setMovementBlock(0);
				vm.startAddress1();
				if (engine.isAbortCurrentThread()) {
					return;
				}
				engine.updatePosition();
				if (mem.getMovementBlock() < 255 && map.canMove(mem.getDungeonX(), mem.getDungeonY(), mem.getDungeonDir())) {
					mem.setLastDungeonX(mem.getDungeonX());
					mem.setLastDungeonY(mem.getDungeonY());
					mem.setDungeonX(mem.getDungeonX() + mem.getDungeonDir().getDeltaX());
					mem.setDungeonY(mem.getDungeonY() + mem.getDungeonDir().getDeltaY());
					engine.updatePosition();
				}
				vm.startSearchLocation();
				if (engine.isAbortCurrentThread()) {
					return;
				}
			} else if (InputAction.TURN_AROUND == action) {
				mem.setDungeonDir(mem.getDungeonDir().getReverse());
			} else if (InputAction.TURN_LEFT == action) {
				mem.setDungeonDir(mem.getDungeonDir().getLeft());
			} else if (InputAction.TURN_RIGHT == action) {
				mem.setDungeonDir(mem.getDungeonDir().getRight());
			}
			engine.updatePosition();
			engine.clearPics();
			engine.setInput(STANDARD);
		}, "VM");
	}
}
