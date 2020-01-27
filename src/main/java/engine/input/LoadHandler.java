package engine.input;

import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMemory;

public class LoadHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			engine.getUi().clearAll();

			final File savesPath = engine.getSavesPath();
			final VirtualMemory memory = engine.getMemory();
			try {
				readMemory(savesPath, memory);
				engine.loadArea(memory.getAreaValue(0), memory.getAreaValue(1), memory.getAreaValue(2));
				engine.loadAreaDecoration(memory.getAreaDecoValue(0), memory.getAreaDecoValue(1), memory.getAreaDecoValue(2));
				System.out.println("Game loaded");
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			engine.showProgramMenu();
		});
	}

	private void readMemory(File savesPath, VirtualMemory memory) throws IOException {
		final File saveGame = new File(savesPath, "savegame.dat");
		if (!saveGame.exists() && !saveGame.canRead()) {
			System.err.println("Cant load");
			return;
		}
		final FileChannel fc = FileChannel.open(saveGame.toPath(), READ);
		try {
			memory.loadFrom(fc);
		} finally {
			fc.close();
		}
	}
}
