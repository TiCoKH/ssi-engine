package data;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class TLBFile extends ContentFile {

	private TLBHLIBBlock content;

	private TLBFile(TLBHLIBBlock content) {
		this.content = content;
	}

	public static TLBFile createFrom(ByteBufferWrapper file) {
		return new TLBFile(TLBHLIBBlock.readFrom(file));
	}

	@Override
	public List<ByteBufferWrapper> getById(int id) {
		return content.getById(id);
	}

	@Override
	public <T extends Content> T getById(int id, @Nonnull Class<T> clazz, @Nonnull ContentType type) {
		return content.getById(id, clazz, type);
	}

	@Override
	public Set<Integer> getIds() {
		return content.getIds();
	}
}
