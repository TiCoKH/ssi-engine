package data.content;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

import data.ContentFile;

public class VGAImageTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/BIGPIC1.DAX");
		Assume.assumeTrue(f.exists());
		ContentFile bigpic1 = ContentFile.create(f).get();
		VGAImage ramAttack = bigpic1.getById(120, VGAImage.class);
		assertThat(ramAttack.size(), is(1));
	}
}
