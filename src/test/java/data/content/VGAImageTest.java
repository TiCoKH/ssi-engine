package data.content;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.Assume;
import org.junit.Test;

import data.DAXFile;

public class VGAImageTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/BIGPIC1.DAX");
		Assume.assumeTrue(f.exists());
		FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		DAXFile bigpic1 = DAXFile.createFrom(c);
		VGAImage ramAttack = bigpic1.getById(120, VGAImage.class);
		assertThat(ramAttack.size(), is(1));
	}
}