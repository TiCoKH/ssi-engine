package data.content;

import javax.annotation.Nonnull;

import common.ByteBufferWrapper;

public class DungeonMap2 extends DungeonMap {

	private int[] decoIds = new int[6];

	public DungeonMap2(@Nonnull ByteBufferWrapper data, @Nonnull DAXContentType type) {
		super(data.getUnsigned(), data.getUnsigned());

		decoIds[0] = data.getUnsigned();
		decoIds[1] = data.getUnsigned();
		decoIds[2] = data.getUnsigned();
		decoIds[3] = data.getUnsigned();
		decoIds[4] = data.getUnsigned();
		decoIds[5] = data.getUnsigned();

		readMap(data.slice());
	}

	public int[] getDecoIds() {
		return decoIds;
	}
}
