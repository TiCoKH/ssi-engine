package data.image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

import data.ContentFile;
import data.ContentType;

public class VGADependentImagesTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/PIC1.DAX");
		Assume.assumeTrue(f.exists());
		ContentFile pic1 = ContentFile.create(f).get();
		VGADependentImages terrines = pic1.getById(32, VGADependentImages.class, ContentType.PIC);
		assertThat(terrines.size(), is(6));
	}
}
