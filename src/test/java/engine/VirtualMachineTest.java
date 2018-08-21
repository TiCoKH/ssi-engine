package engine;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import data.DAXFile;
import data.content.EclProgram;

public class VirtualMachineTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/ECL1.DAX");
		FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		DAXFile<EclProgram> ecls = DAXFile.createFrom(c, EclProgram.class);
		VirtualMachine vm = new VirtualMachine();
		vm.newEcl(ecls.get(0).getObject());
		vm.startInitial();
	}

}
