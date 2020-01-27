package engine.input;

import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMemory;

public class ModeMenuHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		final VirtualMemory memory = engine.getMemory();

		String selectedEntry = action.getName().toString();
		boolean isPlayDemo = "DEMO".equals(selectedEntry);
		String startEcl = engine.getConfig().getModeMenuEntry(selectedEntry);

		memory.setGameSpeed(isPlayDemo ? 9 : 4);
		try {
			memory.setCurrentECL(Integer.parseInt(startEcl));
			if (isPlayDemo) {
				engine.clear();
				engine.loadEcl(false);
			} else {
				engine.setNextTask(engine::showProgramMenu);
			}
		} catch (NumberFormatException e) {
			// TODO in FRUA the entry is not a number
		}
	}

}
