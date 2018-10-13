package engine.input;

import static engine.EngineCallback.InputType.STANDARD;

import data.content.DungeonMap.Direction;
import engine.Engine;
import engine.InputAction;
import engine.VirtualMemory;

public class DungeonMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.getUi().clear();
		engine.setCurrentThread(() -> {
			VirtualMemory memory = engine.getMemory();

			Direction d = memory.getCurrentMapOrient();
			int x = memory.getCurrentMapX();
			int y = memory.getCurrentMapY();
			if (InputAction.MOVE_FORWARD == action) {
				if (engine.canMove(x, y, d)) {
					x += d.getDeltaX();
					y += d.getDeltaY();
					engine.updatePosition(x, y, d);
					engine.getVirtualMachine().startSearchLocation();
				}
				engine.setInput(STANDARD);
				return;
			} else if (InputAction.TURN_AROUND == action) {
				d = d.getReverse();
			} else if (InputAction.TURN_LEFT == action) {
				d = d.getLeft();
			} else if (InputAction.TURN_RIGHT == action) {
				d = d.getRight();
			}
			engine.updatePosition(x, y, d);
			engine.setInput(STANDARD);
		}, "VM");
	}
}
