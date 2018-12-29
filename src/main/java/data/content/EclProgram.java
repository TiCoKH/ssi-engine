package data.content;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class EclProgram extends DAXContent {

	private ByteBufferWrapper code;

	public EclProgram(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		data.rewind();
		int eclId = data.getUnsignedShort();
		if (eclId != 5000) {
			throw new IllegalArgumentException("data is not a valid ecl program");
		}
		code = data.slice();
	}

	public ByteBufferWrapper getCode() {
		return code;
	}
}
