package data;

import static data.TLBHLIBBlock.TLBBlockType.HLIB;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.TreeSet;
import io.vavr.control.Try;

import common.ByteBufferWrapper;

public class TLBHLIBBlock {
	private static final Class<?>[] DATA_ARGUMENT = new Class[] { ByteBufferWrapper.class, ContentType.class };
	private static final Class<?>[] LIST_ARGUMENT = new Class[] { Seq.class, ContentType.class };

	private Seq<ByteBufferWrapper> blocks;
	private HLIBHeader header;

	private TLBHLIBBlock(HLIBHeader header, Seq<ByteBufferWrapper> blocks) {
		this.header = header;
		this.blocks = blocks;
	}

	static TLBHLIBBlock readFrom(ByteBufferWrapper data) {
		final HLIBHeader header = HLIBHeader.readFrom(data);

		final Seq<ByteBufferWrapper> contentBuffers = Array.range(0, header.getContentCount()).map(i -> {
			int contentStart = header.getContentOffset(i).intValue();
			int contentEnd = header.getContentOffset(i + 1).intValue();
			int contentSize = contentEnd - contentStart;
			return data.position(contentStart).slice().limit(contentSize);
		});
		return new TLBHLIBBlock(header, contentBuffers);
	}

	public Seq<ByteBufferWrapper> getById(int id) {
		if (!hasBlockIds()) {
			return blocks;
		}
		return header.mapping.get(id).map(index -> {
			final ByteBufferWrapper data = blocks.get(index);
			if (getContentType() == HLIB) {
				return TLBHLIBBlock.readFrom(data).blocks;
			}
			return Array.of(data);
		}).getOrElse(Array::empty);
	}

	public <T extends Content> Optional<Try<T>> getById(int id, @Nonnull Class<T> clazz, @Nonnull ContentType type) {
		if (!hasBlockIds()) {
			return Optional.of(createInstance(clazz, LIST_ARGUMENT, blocks, type));
		}
		return header.mapping.get(id).map(index -> {
			final ByteBufferWrapper data = blocks.get(index).rewind().slice();
			if (getContentType() == HLIB) {
				return createInstance(clazz, LIST_ARGUMENT, TLBHLIBBlock.readFrom(data).blocks, type);
			}
			return createInstance(clazz, DATA_ARGUMENT, data, type);
		}).toJavaOptional();
	}

	private <T extends Content> Try<T> createInstance(Class<T> clazz, Class<?>[] argument, Object... value) {
		return Try.of(() -> clazz.getConstructor(argument).newInstance(value));
	}

	public Set<Integer> getIds() {
		return TreeSet.ofAll(header.mapping.keySet());
	}

	public TLBBlockType getContentType() {
		return header.getContentType();
	}

	public boolean hasBlockIds() {
		return header.mapping.size() > 1 || header.mapping.getOrElse(0, 0) != 0;
	}

	protected static class HLIBHeader {
		private TLBBlockType contentType;
		private Seq<Long> contentOffsets;
		private int contentCount;
		private Map<Integer, Integer> mapping;

		private HLIBHeader(TLBBlockType contentType, Seq<Long> contentOffsets, int contentCount,
			Map<Integer, Integer> mapping) {
			this.contentType = contentType;
			this.contentOffsets = contentOffsets;
			this.contentCount = contentCount;
			this.mapping = mapping;
		}

		public static HLIBHeader readFrom(ByteBufferWrapper data) {
			byte[] type = new byte[4];

			data.get(type);
			TLBBlockType blockType = TLBBlockType.valueOf(new String(type));
			if (blockType != TLBBlockType.HLIB) {
				throw new IllegalArgumentException("block needs to be of block type HLIB");
			}

			data.getUnsignedInt(); // Block size
			int contentCount = data.getUnsignedShort();
			data.getUnsigned(); // always zero?
			boolean hasTable = data.getUnsigned() == 1;

			data.get(type);
			TLBBlockType contentType = TLBBlockType.valueOf(new String(type));

			Seq<Long> contentOffsets = Array.range(0, contentCount + 1).map(i -> data.getUnsignedInt());

			Map<Integer, Integer> mapping = HashMap.empty();
			if (hasTable) {
				data.position(contentOffsets.get(0).intValue());
				int mappingCount = data.getUnsignedShort();
				mapping = Array.range(0, mappingCount).toMap(i -> {
					int blockId = data.getUnsignedShort();
					int index = data.getUnsignedShort() - 1;
					return new Tuple2<>(blockId, index);
				});
				contentCount--;
				contentOffsets = contentOffsets.removeAt(0);
			} else {
				mapping = mapping.put(0, 0);
			}

			return new HLIBHeader(contentType, contentOffsets, contentCount, mapping);
		}

		public Integer getBlockId(int index) {
			return mapping.getOrElse(index, null);
		}

		public int getContentCount() {
			return contentCount;
		}

		public Long getContentOffset(int index) {
			return contentOffsets.get(index);
		}

		public TLBBlockType getContentType() {
			return contentType;
		}
	}

	public enum TLBBlockType {
		HLIB, TILE, DATA, DIG4;
	}
}
