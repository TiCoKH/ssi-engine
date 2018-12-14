package main;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import common.ByteBufferWrapper;
import data.ContentFile;

public class Extract {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java main.Extract <path or file>");
			System.exit(1);
		}
		File arg = new File(args[0]);
		if (!arg.canRead()) {
			System.err.println("Cant read " + args[0]);
			System.exit(1);
		}
		try {
			if (arg.isDirectory())
				writeDir(arg);
			else if (ContentFile.isKnown(arg))
				writeFile(arg);
			else {
				System.err.println("Unsupported file " + args[0] + ", only DAX files are supported.");
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	private static void writeDir(File d) throws IOException {
		File[] content = d.listFiles((dir, name) -> {
			File f = new File(dir, name);
			return f.isDirectory() || ContentFile.isKnown(f);
		});
		for (int i = 0; i < content.length; i++) {
			if (content[i].isDirectory()) {
				writeDir(content[i]);
			} else if (content[i].isFile()) {
				System.out.println(content[i].getAbsolutePath());
				writeFile(content[i]);
			}
		}
	}

	private static void writeFile(File f) throws IOException {
		File outDir = new File(f.getParentFile(), "RAW");
		outDir.mkdirs();

		Optional<ContentFile> df = ContentFile.create(f);
		if (df.isPresent()) {
			for (Integer id : df.get().getIds()) {
				String inputName = f.getName() + "." + id;
				List<ByteBufferWrapper> dataList = df.get().getById(id);
				for (int i = 0; i < dataList.size(); i++) {
					ByteBufferWrapper buf = dataList.get(i);
					buf.rewind();

					File outFile = new File(outDir, dataList.size() == 1 ? inputName : inputName + "." + i);
					writeOut(buf, outFile);
				}
			}
		}
	}

	private static void writeOut(ByteBufferWrapper data, File outFile) throws IOException {
		System.out.println(outFile.getAbsolutePath());
		try (FileChannel o = FileChannel.open(outFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE)) {
			data.writeTo(o);
		}
	}
}
