package data;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Try;

import common.ByteBufferWrapper;

public class DAXFile extends ContentFile {
	private Map<Integer, ByteBufferWrapper> blocks;

	private DAXFile(Map<Integer, ByteBufferWrapper> blocks) {
		this.blocks = blocks;
	}

	public static DAXFile createFrom(@Nonnull ByteBufferWrapper file) {
		int byteCount = file.getUnsignedShort(0);
		int headerCount = byteCount / 9;

		Map<Integer, ByteBufferWrapper> blocks = HashMap.empty();

		for (int i = 0; i < headerCount; i++) {
			int headerStart = 2 + (i * 9);
			int id = file.getUnsigned(headerStart);
			int offset = (int) file.getUnsignedInt(headerStart + 1);
			int sizeRaw = file.getUnsignedShort(headerStart + 5);
			int sizeCmp = file.getUnsignedShort(headerStart + 7);

			file.position(2 + byteCount + offset);

			blocks = blocks.put(id, uncompress(file.slice().limit(sizeCmp), sizeRaw));
		}
		return new DAXFile(blocks);
	}

	@Override
	public <T extends Content> Optional<Try<T>> getById(int id, @Nonnull Class<T> clazz, @Nonnull ContentType type) {
		return blocks.get(id)
			.map(b -> Try.of(() -> clazz.getConstructor(ByteBufferWrapper.class, ContentType.class)
				.newInstance(b.rewind().slice(), type)))
			.toJavaOptional();
	}

	@Override
	public Seq<ByteBufferWrapper> getById(int id) {
		return blocks.get(id).map(Array::of).getOrElse(Array::empty);
	}

	@Override
	public Set<Integer> getIds() {
		return blocks.keySet();
	}

	private static ByteBufferWrapper uncompress(ByteBufferWrapper compressed, int sizeRaw) {
		ByteBufferWrapper result = ByteBufferWrapper.allocateLE(sizeRaw);

		int in = 0;
		int out = 0;
		compressed.rewind();
		while (in < compressed.limit()) {
			byte next = compressed.get(in++);
			int count = Math.abs(next);
			if (next >= 0) {
				for (int i = 0; i < 1 + count; i++) {
					result.put(out++, compressed.get(in++));
				}
			} else {
				byte repeat = compressed.get(in++);
				for (int i = 0; i < count; i++) {
					result.put(out++, repeat);
				}
			}
		}
		return result;
	}
}
