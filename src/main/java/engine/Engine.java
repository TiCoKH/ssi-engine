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
import data.content.EclProgram;
import data.content.MonocromeSymbols;
import data.content.VGADependentImages;
import data.content.VGAImage;
import engine.opcodes.EclString;
import ui.Borders;
import ui.classic.ClassicRenderer;

public class Engine implements EngineCallback {
	private String gameDir;

	private Map<String, DAXFile> files;

	private ClassicRenderer renderer;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private boolean running;

	private Thread vmThread;
	private Thread gameThread;

	public Engine(String gameDir) throws IOException {
		this.gameDir = gameDir;

		files = new HashMap<>();

		MonocromeSymbols font = readDAXFile("8X8D1.DAX", 201, MonocromeSymbols.class);
		DAXImageContent borders = readDAXFile("BORDERS.DAX", 0, VGAImage.class);
		renderer = new ClassicRenderer(font, borders);
		renderer.setNoPicture(Borders.SCREEN);

		vm = new VirtualMachine(this);
		memory = vm.getMem();

		running = true;

		gameThread = new Thread(() -> {
			while (running) {
				long start = System.currentTimeMillis();
				renderer.increaseText();
				renderer.repaint();
				long end = System.currentTimeMillis();
				if ((end - start) < 16) {
					try {
						Thread.sleep(16 - (end - start));
					} catch (InterruptedException e) {
					}
				}
			}
		}, "Game Loop");
		gameThread.start();

		vmThread = new Thread(() -> {
			try {
				EclProgram demo = readDAXFile("ECL1.DAX", 18, EclProgram.class);
				vm.newEcl(demo);
				vm.startInitial();
				running = false;
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}, "VM");
		vmThread.start();
	}

	public ClassicRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void showPicture(int id) {
		try {
			DAXImageContent smallPic = findImageResource(id, VGADependentImages.class, "PIC1.DAX", "PIC2.DAX", "PIC3.DAX", "PIC4.DAX", "PIC5.DAX",
				"PIC6.DAX", "PIC7.DAX", "PIC8.DAX", "PIC9.DAX");
			if (smallPic != null) {
				renderer.setSmallPicture(smallPic, 0);
				return;
			}
			DAXImageContent bigPic = findImageResource(id, VGAImage.class, "BIGPIC1.DAX", "BIGPIC2.DAX", "BIGPIC3.DAX", "BIGPIC4.DAX", "BIGPIC5.DAX",
				"BIGPIC6.DAX");
			if (bigPic != null) {
				renderer.setBigPicture(bigPic, 0);
				return;
			}
			renderer.setNoPicture(Borders.GAME);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void showText(EclString str) {
		renderer.setText(str);
	}

	private <T extends DAXImageContent> DAXImageContent findImageResource(int id, Class<T> clazz, String... filenames) throws IOException {
		for (int i = 0; i < filenames.length; i++) {
			DAXImageContent dic = readDAXFile(filenames[i], id, clazz);
			if (dic != null) {
				return dic;
			}
		}
		return null;
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
