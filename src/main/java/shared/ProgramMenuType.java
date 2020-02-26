package shared;

public enum ProgramMenuType {
	PROGRAM(MenuType.PROGRAM), //
	PROGRAM_SUB(MenuType.PROGRAM_SUB), //
	;

	private final MenuType menuType;

	private ProgramMenuType(MenuType menuType) {
		this.menuType = menuType;
	}

	public MenuType getMenuType() {
		return menuType;
	}
}
