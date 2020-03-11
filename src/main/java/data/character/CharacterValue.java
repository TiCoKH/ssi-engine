package data.character;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class CharacterValue {
	private static final int MAPPING_INDEX_NONE = -1;

	private final CharacterValueType type;
	private final int fileIndex;
	private final int memoryIndex;
	private final DataType dataType;

	private CharacterValue(CharacterValueType type, int fileIndex, int memoryIndex, DataType dataType) {
		this.type = type;
		this.fileIndex = fileIndex;
		this.memoryIndex = memoryIndex;
		this.dataType = dataType;
	}

	static CharacterValue value(@Nonnull CharacterValueType type, int fileIndex) {
		return new CharacterValue(type, fileIndex, MAPPING_INDEX_NONE, type.getDataType());
	}

	static CharacterValue value(@Nonnull CharacterValueType type, int fileIndex, int memoryIndex) {
		return new CharacterValue(type, fileIndex, memoryIndex, type.getDataType());
	}

	static CharacterValue value(@Nonnull CharacterValueType type, int fileIndex, DataType dataType) {
		return new CharacterValue(type, fileIndex, MAPPING_INDEX_NONE, dataType);
	}

	static CharacterValue value(@Nonnull CharacterValueType type, int fileIndex, int memoryIndex, DataType dataType) {
		return new CharacterValue(type, fileIndex, memoryIndex, dataType);
	}

	public CharacterValueType getType() {
		return type;
	}

	public int read(ByteBufferWrapper file, ByteBufferWrapper memory) {
		if (hasMemoryMapping()) {
			return dataType.read(memory, memoryIndex);
		}
		return dataType.read(file, fileIndex);
	}

	public int readFile(ByteBufferWrapper file) {
		return dataType.read(file, fileIndex);
	}

	public void write(ByteBufferWrapper file, ByteBufferWrapper memory, int value) {
		if (hasMemoryMapping()) {
			dataType.write(memory, memoryIndex, value);
		} else {
			dataType.write(file, fileIndex, value);
		}
	}

	public void writeFile(ByteBufferWrapper file, int value) {
		dataType.write(file, fileIndex, value);
	}

	public void copyToFile(ByteBufferWrapper file, ByteBufferWrapper memory) {
		if (hasMemoryMapping() && type.isReadWrite()) {
			dataType.write(file, fileIndex, dataType.read(memory, memoryIndex));
		}
	}

	public void copyToMemory(ByteBufferWrapper file, ByteBufferWrapper memory) {
		if (hasMemoryMapping()) {
			dataType.write(memory, memoryIndex, dataType.read(file, fileIndex));
		}
	}

	private boolean hasMemoryMapping() {
		return memoryIndex != MAPPING_INDEX_NONE;
	}
}
