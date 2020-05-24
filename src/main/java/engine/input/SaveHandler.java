package engine.input;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

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
				for (int i = 0; i < 8; i++) {
					if (i < mem.getPartyMemberCount())
						writeCharacter(savesPath, mem, i);
					else
						removeCharacter(savesPath, i);
				}
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

	private void writeCharacter(File savesPath, VirtualMemory mem, int index) throws IOException {
		final File charFile = new File(savesPath, String.format("chrdate%d.dat", index + 1));
		final FileChannel fc = FileChannel.open(charFile.toPath(), CREATE, WRITE, TRUNCATE_EXISTING);

		try {
			mem.writePartyMember(index, fc);
		} finally {
			fc.close();
		}
	}

	private void removeCharacter(File savesPath, int index) throws IOException {
		final File charFile = new File(savesPath, String.format("chrdate%d.dat", index + 1));
		Files.deleteIfExists(charFile.toPath());
	}
}
