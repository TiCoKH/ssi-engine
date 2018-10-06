package main;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import common.ByteBufferWrapper;
import data.DAXFile;

public class Extract {

	public static void main(String[] args) {
		File d = new File("/mnt/daten/SSI");
		try {
			writeDir(d);
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	private static void writeDir(File d) throws IOException {
		File[] content = d.listFiles();
		for (int i = 0; i < content.length; i++) {
			if (content[i].isDirectory()) {
				writeDir(content[i]);
			} else if (content[i].isFile() && content[i].getName().endsWith(".DAX")) {
				System.out.println(content[i].getAbsolutePath());
				writeFile(content[i]);
			}
		}
	}

	private static void writeFile(File f) throws IOException {
		FileChannel c = FileChannel.open(f.toPath(), StandardOpenOption.READ);
		DAXFile df = DAXFile.createFrom(c);

		File outDir = new File(f.getParentFile(), "RAW");
		outDir.mkdirs();

		for (Integer id : df.getIds()) {
			ByteBufferWrapper buf = df.getById(id).getUncompressed();
			buf.rewind();

			File outFile = new File(outDir, f.getName() + "." + id);
			System.out.println(outFile.getAbsolutePath());
			try (FileChannel o = FileChannel.open(outFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.WRITE)) {
				buf.writeTo(o);
			}
		}
	}
}
