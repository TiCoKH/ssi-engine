package engine.input;

import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMachine;
import engine.VirtualMemory;

public class GameMenuLOOKHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			engine.clear();

			VirtualMachine vm = engine.getVirtualMachine();
			VirtualMemory mem = engine.getMemory();

			mem.setSearchFlagsTurnLookModeOn();
			vm.startSearchLocation();
			mem.setSearchFlagsTurnLookModeOff();
			if (engine.isAbortCurrentThread()) {
				return;
			}
			engine.updatePosition();
			engine.clearSprite();
			engine.setInputStandard(EngineInputAction.LOOK);
		});
	}

}
