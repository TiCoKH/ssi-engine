package engine.input;

import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import engine.Engine;
import engine.InputAction;
import engine.VirtualMemory;

public class LoadHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		engine.stopCurrentThread();

		File savesPath = engine.getRes().getSavesPath();
		File saveGame = new File(savesPath, "savegame.dat");
		if (!saveGame.exists() && !saveGame.canRead()) {
			System.err.println("Cant load");
			return;
		}
		try {
			FileChannel fc = FileChannel.open(saveGame.toPath(), READ);
			try {
				engine.getMemory().loadFrom(fc);
			} finally {
				fc.close();
			}
			System.out.println("Game loaded");
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

		engine.getUi().clear();

		VirtualMemory m = engine.getMemory();
		engine.loadArea(m.getAreaValue(0), m.getAreaValue(1), m.getAreaValue(2));
		engine.loadAreaDecoration(m.getAreaDecoValue(0), m.getAreaDecoValue(1), m.getAreaDecoValue(2));
		m.setLastECL(m.getCurrentECL());
		engine.loadEcl(m.getCurrentECL());
	}
}
