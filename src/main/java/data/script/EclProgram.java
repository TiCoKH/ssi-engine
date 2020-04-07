package data.script;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;
import data.Content;
import data.ContentType;

public class EclProgram extends Content {

	private ByteBufferWrapper code;

	public EclProgram(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
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
