package shared;

public enum ProgramMenuType {
	PROGRAM(MenuType.PROGRAM);

	private final MenuType menuType;

	private ProgramMenuType(MenuType menuType) {
		this.menuType = menuType;
	}

	public MenuType getMenuType() {
		return menuType;
	}
}
