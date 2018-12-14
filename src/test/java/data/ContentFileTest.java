package data;

import java.io.File;
import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

public class ContentFileTest {

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
			} else if (content[i].isFile() && ContentFile.isKnown(content[i])) {
				System.out.println(content[i].getAbsolutePath());
				ContentFile.create(content[i]);
			}
		}
	}
}
