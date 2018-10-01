package engine.input;

import static engine.EngineCallback.InputType.STANDARD;

import data.content.DungeonMap;
import data.content.DungeonMap.Direction;
import engine.Engine;
import engine.InputAction;
import engine.VirtualMemory;

public class DungeonMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.getRenderer().setInputNone();
		engine.getRenderer().clearText();

		engine.setCurrentThread(() -> {
			engine.getVirtualMachine().startAddress1();
			boolean canMove = true; // TODO

			if (!canMove) {
				engine.setInput(STANDARD);
				return;
			}

			DungeonMap currentMap = engine.getCurrentMap();
			VirtualMemory memory = engine.getMemory();

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

					engine.getVirtualMachine().startSearchLocation();
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
			engine.setInput(STANDARD);
		}, "VM");
	}
}
