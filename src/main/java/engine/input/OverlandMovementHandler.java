package engine.input;

import common.MathUtil;
import engine.Engine;
import engine.InputAction;
import engine.VirtualMemory;

public class OverlandMovementHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.setCurrentThread(() -> {
			VirtualMemory memory = engine.getMemory();

			engine.getVirtualMachine().startMove();
			if (engine.isAbortCurrentThread()) {
				return;
			}

			int x = memory.getOverlandX();
			int y = memory.getOverlandY();
			int newX = x, newY = y;
			if (InputAction.MOVE_OVERLAND_UP == action) {
				newY = y - 1;
			} else if (InputAction.MOVE_OVERLAND_DOWN == action) {
				newY = y + 1;
			} else if (InputAction.MOVE_OVERLAND_LEFT == action) {
				newX = x - 1;
			} else if (InputAction.MOVE_OVERLAND_RIGHT == action) {
				newX = x + 1;
			}
			newX = MathUtil.clamp(newX, 0, 38);
			newY = MathUtil.clamp(newY, 0, 15);
			engine.getMemory().setOverlandX(newX);
			engine.getMemory().setOverlandY(newY);

			engine.getVirtualMachine().startSearchLocation();
			if (engine.isAbortCurrentThread()) {
				return;
			}

			engine.getUi().setInputStandard();
		}, "VM");
	}
}
