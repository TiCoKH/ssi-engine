package ui.classic;

import javax.annotation.Nonnull;
import javax.swing.ActionMap;
import javax.swing.InputMap;

import ui.shared.Menu;

public abstract class AbstractDialogState {
	private final Menu horizontalMenu;
	private ActionMap actionMap;
	private InputMap inputMap;

	protected AbstractDialogState(@Nonnull Menu horizontalMenu, @Nonnull ActionMap actionMap, @Nonnull InputMap inputMap) {
		this.horizontalMenu = horizontalMenu;
		this.actionMap = actionMap;
		this.inputMap = inputMap;
	}

	public abstract Class<? extends AbstractDialogRenderer> getRendererClass();

	@Nonnull
	public Menu getHorizontalMenu() {
		return horizontalMenu;
	}

	public ActionMap getActionMap() {
		return actionMap;
	}

	public InputMap getInputMap() {
		return inputMap;
	}
}
