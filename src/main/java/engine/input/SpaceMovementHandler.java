package engine.input;

import common.MathUtil;
import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMemory;
import shared.InputAction;

public class SpaceMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			engine.clear();

			VirtualMemory memory = engine.getMemory();

			engine.getVirtualMachine().startMove();
			if (engine.isAbortCurrentThread()) {
				return;
			}

			int x = memory.getSpaceX();
			int y = memory.getSpaceY();
			int newX = x, newY = y;
			if (InputAction.FORWARD_UP == action.getName()) {
				newY = y - 1;
			} else if (InputAction.UTURN_DOWN == action.getName()) {
				newY = y + 1;
			} else if (InputAction.TURN_LEFT == action.getName()) {
				newX = x - 1;
			} else if (InputAction.TURN_RIGHT == action.getName()) {
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

			engine.setInputStandard(null);
		});
	}
}
