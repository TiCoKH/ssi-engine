package engine;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import data.DAXFile;
import data.content.DAXContent;
import data.content.DAXImageContent;
import data.content.MonocromeSymbols;
import data.content.VGAImage;
import engine.opcodes.EclString;
import ui.classic.ClassicRenderer;

public class Engine implements EngineCallback {
	private String gameDir;

	private Map<String, DAXFile> files;

	private ClassicRenderer renderer;

	private VirtualMachine vm;
	private VirtualMemory memory;

	public Engine(String gameDir) throws IOException {
		this.gameDir = gameDir;

		files = new HashMap<>();

		MonocromeSymbols font = readDAXFile("8X8D1.DAX", 201, MonocromeSymbols.class);
		DAXImageContent borders = readDAXFile("BORDERS.DAX", 0, VGAImage.class);
		renderer = new ClassicRenderer(font, borders);

		vm = new VirtualMachine(this);
		memory = vm.getMem();
	}

	public ClassicRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void showPicture(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showText(EclString str) {
		// TODO Auto-generated method stub

	}

	private <T extends DAXContent> T readDAXFile(String name, int blockId, Class<T> clazz) throws IOException {
		DAXFile f = files.get(name);
		if (f == null) {
			try (FileChannel c = FileChannel.open(new File(gameDir, name).toPath(), StandardOpenOption.READ)) {
				f = DAXFile.createFrom(c);
				files.put(name, f);
			}
		}
		return f.getById(blockId, clazz);
	}
}
