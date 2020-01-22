package engine.input;

import engine.Engine;
import engine.EngineInputAction;

public class ModeMenuHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		String selectedEntry = action.getName().toString();
		engine.getMemory().setGameSpeed(selectedEntry.equals("DEMO") ? 9 : 4);
		String startEcl = engine.getConfig().getModeMenuEntry(selectedEntry);
		try {
			int ecl = Integer.parseInt(startEcl);
			engine.clear();
			engine.loadEcl(ecl, false);
		} catch (NumberFormatException e) {
			// TODO in FRUA the entry is not a number
		}
	}

}
