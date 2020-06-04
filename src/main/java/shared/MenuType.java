package shared;

public enum MenuType {
	HORIZONTAL, PARTY, PROGRAM, PROGRAM_SUB, VERTICAL;

	public boolean isHorizontalMenu() {
		return this == HORIZONTAL || this == PARTY;
	}

	public boolean isVerticalMenu() {
		return this == VERTICAL;
	}

	public boolean isProgramMenu() {
		return this == PROGRAM || this == PROGRAM_SUB;
	}

	public boolean isPartySelection() {
		return this == PARTY;
	}
}
