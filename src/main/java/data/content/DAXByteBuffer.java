package data.content;

import java.nio.ByteBuffer;

public class DAXByteBuffer extends DAXContent {

	private ByteBuffer data;

	public DAXByteBuffer(ByteBuffer data) {
		this.data = data;
	}

	public ByteBuffer getData() {
		return data;
	}
}
