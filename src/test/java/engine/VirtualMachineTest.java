package engine;

import java.io.File;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;

import common.FileMap;
import data.ContentFile;
import data.content.DAXContentType;
import data.content.EclProgram;
import types.GoldboxString;
import types.MenuType;

public class VirtualMachineTest {

	@Test
	public void test() throws Exception {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/ECL1.DAX");
		Assume.assumeTrue(f.exists());

		FileMap fm = new FileMap(f.getParent());
		EngineConfiguration cfg = new EngineConfiguration(fm);

		ContentFile ecls = ContentFile.create(f).get();
		VirtualMachine vm = new VirtualMachine(new EngineCallback() {
			@Override
			public void clear() {
			}

			@Override
			public void setInputNumber(int maxDigits) {
			}

			@Override
			public void setInputString(int maxLetters) {
			}

			@Override
			public void setMenu(MenuType type, List<InputAction> menuItems, GoldboxString description) {
			}

			@Override
			public void clearSprite() {
			}

			@Override
			public void advanceSprite() {
			}

			@Override
			public void showSprite(int spriteId, int index, int picId) {
			}

			@Override
			public void addText(GoldboxString str, boolean clear) {
			}

			@Override
			public void addNewline() {
			}

			@Override
			public void showPicture(int id) {
			}

			@Override
			public void showPicture(int gameState, int id) {
			}

			@Override
			public void loadEcl(int id) {
			}

			@Override
			public void loadArea(int id1, int id2, int id3) {
			}

			@Override
			public void loadAreaDecoration(int id1, int id2, int id3) {
			}

			@Override
			public void updatePosition() {
			}

			@Override
			public void delayCurrentThread() {
			}
		}, new VirtualMemory(cfg), cfg.getCodeBase());
		vm.newEcl(ecls.getById(16, EclProgram.class, DAXContentType.ECL));

		System.out.println("Init:");
		vm.startInit();
		System.out.println("Move:");
		vm.startMove();
		System.out.println("SearchLocation:");
		vm.startSearchLocation();
		System.out.println("Rest:");
		vm.startRest();
		System.out.println("RestInterruption:");
		vm.startRestInterruption();
	}

}
