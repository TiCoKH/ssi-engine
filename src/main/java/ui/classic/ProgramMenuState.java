package ui.classic;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.swing.ActionMap;
import javax.swing.InputMap;

import shared.GoldboxString;
import shared.InputAction;
import shared.ProgramMenuType;
import shared.ViewGlobalData;
import ui.shared.Menu;

public class ProgramMenuState extends AbstractDialogState {

	private final InputAction menuSelect;
	private final Menu programMenu;
	private final ProgramMenuType menuType;
	private final Optional<ViewGlobalData> globalData;

	public ProgramMenuState(@Nonnull Optional<ViewGlobalData> globalData, @Nonnull Menu horizontalMenu, @Nonnull ActionMap actionMap,
		@Nonnull InputMap inputMap, @Nonnull InputAction menuSelect, @Nonnull Menu programMenu, @Nonnull ProgramMenuType menuType) {

		super(horizontalMenu, actionMap, inputMap);
		this.globalData = globalData;
		this.menuSelect = menuSelect;
		this.programMenu = programMenu;
		this.menuType = menuType;
	}

	@Override
	public Class<? extends AbstractDialogRenderer> getRendererClass() {
		return ProgramMenuRenderer.class;
	}

	@Nonnull
	public InputAction getMenuSelect() {
		return menuSelect;
	}

	@Nonnull
	public Menu getProgramMenu() {
		return programMenu;
	}

	public Optional<GoldboxString> getProgramMenuDescription() {
		return programMenu.getDescription();
	}

	@Nonnull
	public ProgramMenuType getMenuType() {
		return menuType;
	}

	public Optional<ViewGlobalData> getGlobalData() {
		return globalData;
	}
}
