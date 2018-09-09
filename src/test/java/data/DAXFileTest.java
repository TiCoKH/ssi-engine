package data;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.Assume;
import org.junit.Test;

public class DAXFileTest {

	@Test
	public void test() throws IOException {
		File d = new File("/mnt/daten/SSI");
		Assume.assumeTrue(d.exists());
		testDir(d);
	}

	private void testDir(File d) throws IOException {
		File[] content = d.listFiles();
		for (int i = 0; i < content.length; i++) {
			if (content[i].isDirectory()) {
				testDir(content[i]);
			} else if (content[i].isFile() && content[i].getName().endsWith(".DAX")) {
				System.out.println(content[i].getAbsolutePath());
				testFile(content[i].toPath());
			}
		}
	}

	private void testFile(Path p) throws IOException {
		FileChannel c = FileChannel.open(p, StandardOpenOption.READ);
		DAXFile df = DAXFile.createFrom(c);
	}
}
