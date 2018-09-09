package engine;

import static data.content.DAXContentType.BIGPIC;
import static data.content.DAXContentType.PIC;

import java.io.IOException;

import data.content.DAXImageContent;
import data.content.EclProgram;
import engine.opcodes.EclString;
import ui.Borders;
import ui.classic.ClassicRenderer;

public class Engine implements EngineCallback, RendererCallback {
	private EngineResources res;
	private ClassicRenderer renderer;

	private VirtualMachine vm;
	private VirtualMemory memory;

	private boolean running;

	private Thread vmThread;
	private Thread gameThread;

	public Engine(String gameDir) throws IOException {
		res = new EngineResources(gameDir);

		renderer = new ClassicRenderer(this, res.getFont(), res.getBorders());
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
				EclProgram demo = res.load("ECL1.DAX", 18, EclProgram.class);
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
		if (id == 255 || id == -1) {
			renderer.setNoPicture(Borders.GAME);
			return;
		}
		try {
			DAXImageContent smallPic = res.findImage(id, PIC);
			if (smallPic != null) {
				renderer.setSmallPicture(smallPic, 0);
				return;
			}
			DAXImageContent bigPic = res.findImage(id, BIGPIC);
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
		synchronized (vm) {
			renderer.setText(str);
			try {
				vm.wait();
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public void textDisplayFinished() {
		synchronized (vm) {
			vm.notify();
		}
	}
}
