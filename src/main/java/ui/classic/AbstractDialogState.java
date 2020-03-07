package ui.classic;

import javax.annotation.Nonnull;

import ui.shared.Menu;

public abstract class AbstractDialogState {
	private final Menu horizontalMenu;

	public AbstractDialogState(@Nonnull Menu horizontalMenu) {
		this.horizontalMenu = horizontalMenu;
	}

	public abstract Class<? extends AbstractDialogRenderer> getRendererClass();

	@Nonnull
	public Menu getHorizontalMenu() {
		return horizontalMenu;
	}
}
