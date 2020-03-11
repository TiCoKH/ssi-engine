package data.character;

import io.vavr.Function2;
import io.vavr.Function3;

import common.ByteBufferWrapper;

public enum DataType {
	BYTE(ByteBufferWrapper::getUnsigned, (buf, index, val) -> buf.put(index, val.byteValue())), //
	SHORT(ByteBufferWrapper::getUnsignedShort, (buf, index, val) -> buf.putShort(index, val.shortValue())), //
	INT(ByteBufferWrapper::getInt, (buf, index, val) -> buf.putInt(index, val)), //
	;

	private final Function2<ByteBufferWrapper, Integer, Integer> reader;
	private final Function3<ByteBufferWrapper, Integer, Integer, ByteBufferWrapper> writer;

	private DataType(Function2<ByteBufferWrapper, Integer, Integer> reader,
		Function3<ByteBufferWrapper, Integer, Integer, ByteBufferWrapper> writer) {
		this.reader = reader;
		this.writer = writer;
	}

	public int read(ByteBufferWrapper buf, Integer index) {
		return reader.apply(buf, index);
	}

	public void write(ByteBufferWrapper buf, Integer index, int value) {
		writer.apply(buf, index, value);
	}
}
