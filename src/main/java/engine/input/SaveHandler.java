package engine.input;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMemory;

public class SaveHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			File savesPath = engine.getSavesPath();
			if (!savesPath.exists()) {
				boolean result = savesPath.mkdirs();
				if (!result) {
					System.err.println("Saving not possible, directory wasnt created: " + savesPath.getAbsolutePath());
					return;
				}
			}

			final VirtualMemory mem = engine.getMemory();
			try {
				writeMemory(savesPath, mem);
				System.out.println("Game saved");
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
			engine.setInputStandard(null);
		});
	}

	private void writeMemory(File savesPath, VirtualMemory mem) throws IOException {
		final File saveGame = new File(savesPath, "savegame.dat");
		final FileChannel fc = FileChannel.open(saveGame.toPath(), CREATE, WRITE, TRUNCATE_EXISTING);
		try {
			mem.saveTo(fc);
		} finally {
			fc.close();
		}
	}
}
