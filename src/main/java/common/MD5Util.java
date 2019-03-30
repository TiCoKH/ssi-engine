package common;

import static java.nio.file.StandardOpenOption.READ;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

	private MD5Util() {
	}

	public static String getMd5For(File f) throws IOException {
		ByteBufferWrapper buf = ByteBufferWrapper.allocate((int) f.length());

		try (FileChannel fc = FileChannel.open(f.toPath(), READ)) {
			buf.readFrom(fc);
		}

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(buf.array());

			return String.format("%032x", new BigInteger(1, md.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(System.err);
			return "";
		}
	}
}
