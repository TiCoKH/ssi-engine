package ui;

public enum BorderSymbols {
	EM(-1), // Empty
	UL(0), // Upper left corner
	UR(1), // Upper right corner
	LR(2), // Lower right corner
	LL(3), // Lower left corner
	HO(4), // Horizontal
	VE(5), // Vertical
	S3(6), // T piece pointing down
	W3(7), // T piece pointing left
	N3(8), // T piece pointing up
	E3(9), // T piece pointing right
	OH(10), // Horizontal ornamented
	OV(11); // Vertical ornamented

	private int index;

	private BorderSymbols(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
}
