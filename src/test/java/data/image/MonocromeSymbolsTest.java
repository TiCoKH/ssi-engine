package data.image;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

import data.ContentFile;
import data.ContentType;

public class MonocromeSymbolsTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/8X8D1.DAX");
		Assume.assumeTrue(f.exists());
		ContentFile symbols = ContentFile.create(f).get();
		MonocromeSymbols ms = symbols.getById(201, MonocromeSymbols.class, ContentType._8X8D).get();
		assertThat(ms.size(), is(256));
	}

}
