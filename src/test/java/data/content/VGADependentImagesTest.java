package data.content;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import data.DAXFile;

public class VGADependentImagesTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/PIC1.DAX");
		FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		DAXFile pic1 = DAXFile.createFrom(c);
		VGADependentImages terrines = pic1.getById(32, VGADependentImages.class);
		assertThat(terrines.size(), is(6));
	}
}
