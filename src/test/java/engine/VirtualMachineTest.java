package engine;

import java.io.File;

import io.vavr.collection.Seq;

import org.junit.Assume;
import org.junit.Test;

import common.FileMap;
import data.ContentFile;
import data.ContentType;
import data.script.EclProgram;
import engine.script.EclInstruction;
import shared.GoldboxString;
import shared.InputAction;
import shared.MenuType;

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
			public void setECLMenu(MenuType type, Seq<GoldboxString> menuItems, GoldboxString description) {
			}

			@Override
			public void setMenu(MenuType type, Seq<InputAction> menuItems, GoldboxString description) {
			}

			@Override
			public void clearSprite() {
			}

			@Override
			public void advanceSprite() {
			}

			@Override
			public int showSprite(int spriteId, int index, int picId) {
				return 0;
			}

			@Override
			public void addText(GoldboxString str, boolean clear) {
			}

			@Override
			public void addRunicText(GoldboxString str) {
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

			@Override
			public void addNpc(int id) {
			}

			@Override
			public void removeNpc(int index) {
			}
		}, new VirtualMemory(cfg), cfg.getCodeBase());
		EclInstruction.configOpCodes(cfg.getOpCodes());
		vm.newEcl(ecls.getById(16, EclProgram.class, ContentType.ECL).get().get());

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
