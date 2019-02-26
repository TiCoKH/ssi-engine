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
		File savesPath = engine.getSavesPath();
		File saveGame = new File(savesPath, "savegame.dat");
		if (!saveGame.exists() && !saveGame.canRead()) {
			System.err.println("Cant load");
			return;
		}

		engine.setNextTask(() -> {
			engine.clear();

			try (FileChannel fc = FileChannel.open(saveGame.toPath(), READ)) {
				VirtualMemory m = engine.getMemory();
				m.loadFrom(fc);
				engine.loadArea(m.getAreaValue(0), m.getAreaValue(1), m.getAreaValue(2));
				engine.loadAreaDecoration(m.getAreaDecoValue(0), m.getAreaDecoValue(1), m.getAreaDecoValue(2));
				engine.loadEcl(m.getCurrentECL(), false);
				System.out.println("Game loaded");
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		});
	}
}
