package data.content;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import data.DAXFile;

public class MonocromeSymbolsTest {

	@Test
	public void test() throws IOException {
		File f = new File("/mnt/daten/SSI/BUCK11_0.EN/8X8D1.DAX");
		FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		DAXFile symbols = DAXFile.createFrom(c);
		MonocromeSymbols ms = symbols.getById(201, MonocromeSymbols.class);
	}

}
