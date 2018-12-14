package data.content;

import java.io.File;
import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

import data.ContentFile;

public class MonocromeSymbolsTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/8X8D1.DAX");
		Assume.assumeTrue(f.exists());
		ContentFile symbols = ContentFile.create(f).get();
		MonocromeSymbols ms = symbols.getById(201, MonocromeSymbols.class);
	}

}
