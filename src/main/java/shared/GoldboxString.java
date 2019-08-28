package shared;

public abstract class GoldboxString {

	public abstract int getLength();

	public abstract byte getChar(int index);

	public char getCharAsAscii(int index) {
		return toASCII(getChar(index));
	}

	protected static char toASCII(byte c) {
		return (char) (c > 0x1F ? c : c + 0x40);
	}

	protected static byte fromASCII(char c) {
		return (byte) (c >= 0x40 && c <= 0x5F ? c - 0x40 : c);
	}
}
