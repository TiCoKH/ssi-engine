package engine.debug;

public enum CodeSection {
	onMove(0), onSearchLocation(4), onRest(8), onRestInterruption(12), onInit(16);

	private int startOffset;

	private CodeSection(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getStartOffset() {
		return startOffset;
	}
}
