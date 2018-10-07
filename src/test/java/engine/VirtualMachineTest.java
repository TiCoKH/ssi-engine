package engine;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;

import data.DAXFile;
import data.content.EclProgram;
import engine.opcodes.EclString;

public class VirtualMachineTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/ECL1.DAX");
		Assume.assumeTrue(f.exists());

		FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		DAXFile ecls = DAXFile.createFrom(c);
		VirtualMachine vm = new VirtualMachine(new EngineCallback() {
			@Override
			public void setInput(InputType inputType) {
			}

			@Override
			public void setMenu(List<InputAction> action) {
			}

			@Override
			public void clearSprite() {
			}

			@Override
			public void advanceSprite() {
			}

			@Override
			public void showSprite(int id, int index) {
			}

			@Override
			public void addText(EclString str, boolean clear) {
			}

			@Override
			public void addNewline() {
			}

			@Override
			public void showPicture(int id) {
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
		});
		vm.newEcl(ecls.getById(16, EclProgram.class));
		System.out.println("Initial:");
		vm.startInitial();
		System.out.println("Address:");
		vm.startAddress1();
		System.out.println("Search:");
		vm.startSearchLocation();
		System.out.println("PreCamp:");
		vm.startPreCampCheck();
		System.out.println("CampInterrupt:");
		vm.startCampInterrupted();
	}

}
