package engine.input;

import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;

import data.character.AbstractCharacter;
import engine.Engine;
import engine.EngineInputAction;
import engine.VirtualMemory;
import engine.character.CharacterSheetImpl;

public class LoadHandler implements InputHandler {

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		engine.setNextTask(() -> {
			engine.getUi().clearAll();

			final File savesPath = engine.getSavesPath();
			final VirtualMemory memory = engine.getMemory();
			try {
				memory.clearParty();
				readMemory(savesPath, memory);
				for (int i = 0; i < 8; i++) {
					if (!readCharacter(engine, savesPath, i)) {
						break;
					}
				}
				engine.loadArea(memory.getAreaValue(0), memory.getAreaValue(1), memory.getAreaValue(2));
				engine.loadAreaDecoration(memory.getAreaDecoValue(0), memory.getAreaDecoValue(1), memory.getAreaDecoValue(2));
				System.out.println("Game loaded");
			} catch (Exception e) {
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

	private boolean readCharacter(Engine engine, File savesPath, int index) throws Exception {
		final File charFile = new File(savesPath, String.format("chrdate%d.dat", index + 1));
		if (!charFile.exists() && !charFile.canRead()) {
			return false;
		}
		final FileChannel fc = FileChannel.open(charFile.toPath(), READ);
		final AbstractCharacter c = engine.getPlayerDataFactory().loadCharacter(fc);
		engine.getMemory().addPartyMember(new CharacterSheetImpl(engine.getConfig().getFlavor(), c));
		return true;
	}
}
