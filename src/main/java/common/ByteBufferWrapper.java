package common;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import javax.annotation.Nonnull;

public class ByteBufferWrapper {

	private ByteBuffer buf;

	public ByteBufferWrapper(@Nonnull ByteBuffer buf) {
		this.buf = buf;
	}

	public static ByteBufferWrapper allocate(int capacity) {
		ByteBuffer buf = ByteBuffer.allocate(capacity);
		return new ByteBufferWrapper(buf);
	}

	public static ByteBufferWrapper allocateLE(int capacity) {
		ByteBuffer buf = ByteBuffer.allocate(capacity).order(LITTLE_ENDIAN);
		return new ByteBufferWrapper(buf);
	}

	public static ByteBufferWrapper allocateDirect(int capacity) {
		ByteBuffer buf = ByteBuffer.allocateDirect(capacity);
		return new ByteBufferWrapper(buf);
	}

	public ByteBufferWrapper readFrom(FileChannel c) throws IOException {
		c.read(buf);
		return this;
	}

	public ByteBufferWrapper writeTo(FileChannel c) throws IOException {
		c.write(buf);
		return this;
	}

	public final byte[] array() {
		return buf.array();
	}

	public final int arrayOffset() {
		return buf.arrayOffset();
	}

	public final int capacity() {
		return buf.capacity();
	}

	public final ByteBufferWrapper clear() {
		buf.clear();
		return this;
	}

	public ByteBufferWrapper compact() {
		buf.compact();
		return this;
	}

	public int compareTo(ByteBufferWrapper that) {
		return buf.compareTo(that.buf);
	}

	public ByteBufferWrapper duplicate() {
		return new ByteBufferWrapper(buf.duplicate().order(buf.order()));
	}

	public final ByteBufferWrapper flip() {
		buf.flip();
		return this;
	}

	public byte get() {
		return buf.get();
	}

	public ByteBufferWrapper get(byte[] dst) {
		buf.get(dst);
		return this;
	}

	public ByteBufferWrapper get(byte[] dst, int offset, int length) {
		buf.get(dst, offset, length);
		return this;
	}

	public byte get(int index) {
		return buf.get(index);
	}

	public char getChar() {
		return buf.getChar();
	}

	public char getChar(int index) {
		return buf.getChar(index);
	}

	public double getDouble() {
		return buf.getDouble();
	}

	public double getDouble(int index) {
		return buf.getDouble(index);
	}

	public float getFloat() {
		return buf.getFloat();
	}

	public float getFloat(int index) {
		return buf.getFloat(index);
	}

	public int getInt() {
		return buf.getInt();
	}

	public int getInt(int index) {
		return buf.getInt(index);
	}

	public long getLong() {
		return buf.getLong();
	}

	public long getLong(int index) {
		return buf.getLong(index);
	}

	public short getShort() {
		return buf.getShort();
	}

	public short getShort(int index) {
		return buf.getShort(index);
	}

	public int getUnsigned() {
		return buf.get() & 0xFF;
	}

	public int getUnsigned(int index) {
		return buf.get(index) & 0xFF;
	}

	public long getUnsignedInt() {
		return buf.getInt() & 0xFFFFFFFF;
	}

	public long getUnsignedInt(int index) {
		return buf.getInt(index) & 0xFFFFFFFF;
	}

	public int getUnsignedShort() {
		return buf.getShort() & 0xFFFF;
	}

	public int getUnsignedShort(int index) {
		return buf.getShort(index) & 0xFFFF;
	}

	public final boolean hasArray() {
		return buf.hasArray();
	}

	public final boolean hasRemaining() {
		return buf.hasRemaining();
	}

	public boolean isDirect() {
		return buf.isDirect();
	}

	public boolean isReadOnly() {
		return buf.isReadOnly();
	}

	public final int limit() {
		return buf.limit();
	}

	public final ByteBufferWrapper limit(int newLimit) {
		buf.limit(newLimit);
		return this;
	}

	public final ByteBufferWrapper mark() {
		buf.mark();
		return this;
	}

	public final ByteOrder order() {
		return buf.order();
	}

	public final ByteBufferWrapper order(ByteOrder bo) {
		buf.order(bo);
		return this;
	}

	public final int position() {
		return buf.position();
	}

	public final ByteBufferWrapper position(int newPosition) {
		buf.position(newPosition);
		return this;
	}

	public ByteBufferWrapper put(byte b) {
		buf.put(b);
		return this;
	}

	public final ByteBufferWrapper put(byte[] src) {
		buf.put(src);
		return this;
	}

	public ByteBufferWrapper put(byte[] src, int offset, int length) {
		buf.put(src, offset, length);
		return this;
	}

	public ByteBufferWrapper put(ByteBufferWrapper src) {
		buf.put(src.buf);
		return this;
	}

	public ByteBufferWrapper put(int index, byte b) {
		buf.put(index, b);
		return this;
	}

	public ByteBufferWrapper putChar(char value) {
		buf.putChar(value);
		return this;
	}

	public ByteBufferWrapper putChar(int index, char value) {
		buf.putChar(index, value);
		return this;
	}

	public ByteBufferWrapper putDouble(double value) {
		buf.putDouble(value);
		return this;
	}

	public ByteBufferWrapper putDouble(int index, double value) {
		buf.putDouble(index, value);
		return this;
	}

	public ByteBufferWrapper putFloat(float value) {
		buf.putFloat(value);
		return this;
	}

	public ByteBufferWrapper putFloat(int index, float value) {
		buf.putFloat(index, value);
		return this;
	}

	public ByteBufferWrapper putInt(int value) {
		buf.putInt(value);
		return this;
	}

	public ByteBufferWrapper putInt(int index, int value) {
		buf.putInt(index, value);
		return this;
	}

	public ByteBufferWrapper putLong(int index, long value) {
		buf.putLong(index, value);
		return this;
	}

	public ByteBufferWrapper putLong(long value) {
		buf.putLong(value);
		return this;
	}

	public ByteBufferWrapper putShort(int index, short value) {
		buf.putShort(index, value);
		return this;
	}

	public ByteBufferWrapper putShort(short value) {
		buf.putShort(value);
		return this;
	}

	public final int remaining() {
		return buf.remaining();
	}

	public final ByteBufferWrapper reset() {
		buf.reset();
		return this;
	}

	public final ByteBufferWrapper rewind() {
		buf.rewind();
		return this;
	}

	public ByteBufferWrapper slice() {
		return new ByteBufferWrapper(buf.slice().order(buf.order()));
	}
}
