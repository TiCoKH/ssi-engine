package engine.input;

import static engine.EngineCallback.InputType.STANDARD;

import common.MathUtil;
import engine.Engine;
import engine.InputAction;
import engine.VirtualMemory;

public class SpaceMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.setCurrentThread(() -> {
			VirtualMemory memory = engine.getMemory();

			engine.getVirtualMachine().startAddress1();
			if (engine.isAbortCurrentThread()) {
				return;
			}

			int x = memory.getSpaceX();
			int y = memory.getSpaceY();
			int newX = x, newY = y;
			if (InputAction.MOVE_SPACE_UP == action) {
				newY = y - 1;
			} else if (InputAction.MOVE_SPACE_DOWN == action) {
				newY = y + 1;
			} else if (InputAction.MOVE_SPACE_LEFT == action) {
				newX = x - 1;
			} else if (InputAction.MOVE_SPACE_RIGHT == action) {
				newX = x + 1;
			}
			// Not moving into the sun
			if (newX == 11 && newY == 10) {
				newX = x;
				newY = y;
			}
			newX = MathUtil.clamp(newX, 0, 21);
			newY = MathUtil.clamp(newY, 0, 21);
			engine.getMemory().setSpaceX(newX);
			engine.getMemory().setSpaceY(newY);

			engine.getVirtualMachine().startSearchLocation();
			if (engine.isAbortCurrentThread()) {
				return;
			}

			engine.setInput(STANDARD);
		}, "VM");
	}
}
