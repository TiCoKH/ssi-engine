package engine.input;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import engine.Engine;
import engine.InputAction;

public class SaveHandler implements InputHandler {

	@Override
	public void handle(Engine engine, InputAction action) {
		File savesPath = engine.getSavesPath();
		if (!savesPath.exists()) {
			boolean result = savesPath.mkdirs();
			if (!result) {
				System.err.println("Saving not possible, directory wasnt created: " + savesPath.getAbsolutePath());
				return;
			}
		}
		File saveGame = new File(savesPath, "savegame.dat");
		try {
			FileChannel fc = FileChannel.open(saveGame.toPath(), CREATE, WRITE, TRUNCATE_EXISTING);
			try {
				engine.getMemory().saveTo(fc);
			} finally {
				fc.close();
			}
			System.out.println("Game saved");
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
}
