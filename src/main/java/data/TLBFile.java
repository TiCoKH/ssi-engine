package data;

import java.util.Optional;

import javax.annotation.Nonnull;

import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Try;

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
	public Seq<ByteBufferWrapper> getById(int id) {
		return content.getById(id);
	}

	@Override
	public <T extends Content> Optional<Try<T>> getById(int id, @Nonnull Class<T> clazz, @Nonnull ContentType type) {
		return content.getById(id, clazz, type);
	}

	@Override
	public Set<Integer> getIds() {
		return content.getIds();
	}
}
