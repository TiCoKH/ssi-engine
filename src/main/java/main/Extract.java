package main;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.File;

import io.vavr.collection.Seq;
import io.vavr.control.Try;

import common.ByteBufferWrapper;
import data.ContentFile;

public class Extract {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java main.Extract <path or file>...");
			System.exit(1);
		}
		for (String argX : args) {
			File arg = new File(argX);
			if (!arg.canRead()) {
				System.err.println("Cant read " + argX);
				System.exit(1);
			}
			try {
				if (arg.isDirectory())
					writeDir(arg);
				else if (ContentFile.isKnown(arg))
					writeFile(arg).get();
				else {
					System.err.println("Unsupported file " + argX + ", only DAX or (G|T)LB files are supported.");
					System.exit(1);
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	private static void writeDir(File d) {
		File[] content = d.listFiles((dir, name) -> {
			File f = new File(dir, name);
			return f.isDirectory() || ContentFile.isKnown(f);
		});
		for (int i = 0; i < content.length; i++) {
			if (content[i].isDirectory()) {
				writeDir(content[i]);
			} else if (content[i].isFile()) {
				System.out.println(content[i].getAbsolutePath());
				writeFile(content[i]).get();
			}
		}
	}

	private static Try<ContentFile> writeFile(File f) {
		File outDir = new File(f.getParentFile(), "RAW");
		outDir.mkdirs();

		return ContentFile.create(f).andThenTry(cf -> {
			for (Integer id : cf.getIds()) {
				String inputName = f.getName() + "." + id;
				Seq<ByteBufferWrapper> dataList = cf.getById(id);
				for (int i = 0; i < dataList.size(); i++) {
					ByteBufferWrapper buf = dataList.get(i);
					buf.rewind();

					File outFile = new File(outDir, dataList.size() == 1 ? inputName : inputName + "." + i);
					writeOut(buf, outFile).get();
				}
			}
		});
	}

	private static Try<ByteBufferWrapper> writeOut(ByteBufferWrapper data, File outFile) {
		System.out.println(outFile.getAbsolutePath());
		return Try.withResources(() -> open(outFile.toPath(), CREATE, TRUNCATE_EXISTING, WRITE)).of(data::writeTo);
	}
}
