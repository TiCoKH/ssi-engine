package ui;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import data.DAXBlock;
import data.DAXFile;
import data.content.DAXByteBuffer;

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
		DAXFile<DAXByteBuffer> df = DAXFile.createFrom(c, DAXByteBuffer.class);

		File outDir = new File(f.getParentFile(), "RAW");
		outDir.mkdirs();

		for (DAXBlock<DAXByteBuffer> b : df) {
			int id = b.getId() & 0xFF;

			ByteBuffer buf = b.getObject().getData();
			buf.rewind();

			File outFile = new File(outDir, f.getName() + "." + id);
			System.out.println(outFile.getAbsolutePath());
			try (FileChannel o = FileChannel.open(outFile.toPath(), StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
				o.write(buf);
			}
		}
	}
}
