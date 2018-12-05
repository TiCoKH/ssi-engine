package data;

import static data.TLBHLIBBlock.TLBBlockType.HLIB;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import common.ByteBufferWrapper;
import data.content.DAXContent;
import data.content.DAXContentType;

public class TLBHLIBBlock {
	private static final Class<?>[] DATA_ARGUMENT = new Class[] { ByteBufferWrapper.class, DAXContentType.class };
	private static final Class<?>[] LIST_ARGUMENT = new Class[] { List.class, DAXContentType.class };

	private List<ByteBufferWrapper> blocks;
	private HLIBHeader header;

	private TLBHLIBBlock(HLIBHeader header, List<ByteBufferWrapper> blocks) {
		this.header = header;
		this.blocks = blocks;
	}

	static TLBHLIBBlock readFrom(ByteBufferWrapper data) {
		HLIBHeader header = HLIBHeader.readFrom(data);

		List<ByteBufferWrapper> contentBuffers = new ArrayList<>();
		for (int i = 0; i < header.getContentCount(); i++) {

			int contentStart = header.getContentOffset(i).intValue();
			int contentEnd = header.getContentOffset(i + 1).intValue();
			int contentSize = contentEnd - contentStart;

			contentBuffers.add(data.position(contentStart).slice().limit(contentSize));
		}
		return new TLBHLIBBlock(header, ImmutableList.copyOf(contentBuffers));
	}

	public List<ByteBufferWrapper> getById(int id) {
		if (!hasBlockIds()) {
			return blocks;
		}
		int index = header.mapping.get(id);
		ByteBufferWrapper data = blocks.get(index);
		if (getContentType() == HLIB) {
			return TLBHLIBBlock.readFrom(data).blocks;
		}
		return ImmutableList.of(data);
	}

	public <T extends DAXContent> T getById(int id, @Nonnull Class<T> clazz, @Nonnull DAXContentType type) {
		if (!hasBlockIds()) {
			return createInstance(clazz, LIST_ARGUMENT, blocks, type);
		}
		if (!header.mapping.containsKey(id)) {
			return null;
		}
		int index = header.mapping.get(id);
		ByteBufferWrapper data = blocks.get(index).rewind();
		if (getContentType() == HLIB) {
			return createInstance(clazz, LIST_ARGUMENT, TLBHLIBBlock.readFrom(data).blocks, type);
		}
		return createInstance(clazz, DATA_ARGUMENT, data, type);
	}

	private <T extends DAXContent> T createInstance(Class<T> clazz, Class<?>[] argument, Object... value) {
		try {
			return clazz.getConstructor(argument).newInstance(value);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
			| SecurityException e) {
			e.printStackTrace(System.err);
		}
		return null;
	}

	public Set<Integer> getIds() {
		return header.mapping.keySet();
	}

	public TLBBlockType getContentType() {
		return header.getContentType();
	}

	public boolean hasBlockIds() {
		return header.mapping.size() > 1 || header.mapping.getOrDefault(0, 0) != 0;
	}

	protected static class HLIBHeader {
		private TLBBlockType contentType;
		private List<Long> contentOffsets;
		private int contentCount;
		private Map<Integer, Integer> mapping;

		private HLIBHeader(TLBBlockType contentType, List<Long> contentOffsets, int contentCount, Map<Integer, Integer> mapping) {
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

			List<Long> contentOffsets = new ArrayList<>(contentCount);
			for (int i = 0; i < contentCount + 1; i++) {
				contentOffsets.add(data.getUnsignedInt());
			}

			Map<Integer, Integer> mapping = new HashMap<>();
			if (hasTable) {
				data.position(contentOffsets.get(0).intValue());
				int mappingCount = data.getUnsignedShort();
				for (int i = 0; i < mappingCount; i++) {
					int blockId = data.getUnsignedShort();
					int index = data.getUnsignedShort() - 1;
					mapping.put(blockId, index);
				}
				contentCount--;
				contentOffsets.remove(0);
			} else {
				mapping.put(0, 0);
			}

			return new HLIBHeader(contentType, contentOffsets, contentCount, mapping);
		}

		public Integer getBlockId(int index) {
			return mapping.get(index);
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

	public static enum TLBBlockType {
		HLIB, TILE, DATA, DIG4;
	}
}
