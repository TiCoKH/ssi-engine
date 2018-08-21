package data.content;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EclProgram extends DAXContent {

	private ByteBuffer code;

	public EclProgram(ByteBuffer data) {
		data.rewind();
		int eclId = data.getShort() & 0xFFFF;
		if (eclId != 5000) {
			throw new IllegalArgumentException("data is not a valid ecl program");
		}
		code = data.slice();
		code.order(ByteOrder.LITTLE_ENDIAN);
	}

	public ByteBuffer getCode() {
		return code;
	}
}
