package data.content;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class EclProgram extends DAXContent {

	private ByteBufferWrapper code;

	public EclProgram(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		int eclId = data.getUnsignedShort();
		if (eclId != 5000) {
			data.rewind();
		}
		code = data.slice();
	}

	public ByteBufferWrapper getCode() {
		return code;
	}
}
