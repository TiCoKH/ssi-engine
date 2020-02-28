package ui.classic;

import static shared.FontColor.GAME_NAME;
import static shared.FontColor.INTENSE;
import static shared.FontColor.NORMAL;
import static shared.FontColor.SHORTCUT;
import static ui.shared.UIFrame.SCREEN;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import ui.UISettings;
import ui.shared.Menu;
import ui.shared.resource.UIResourceManager;

public class ProgramMenuRenderer extends AbstractDialogRenderer {

	public ProgramMenuRenderer(@Nonnull UISettings settings, @Nonnull UIResourceManager resman, @Nonnull AbstractFrameRenderer frameRenderer) {
		super(settings, resman, frameRenderer);
	}

	@Override
	public void render(@Nonnull Graphics2D g2d, @Nonnull AbstractDialogState state) {
		final ProgramMenuState menuState = (ProgramMenuState) state;
		renderFrame(g2d, SCREEN);
		renderHorizontalMenu(g2d, menuState.getHorizontalMenu());
		switch (menuState.getMenuType()) {
			case PROGRAM:
				renderParty(g2d, menuState.getGlobalData(), 1);
				renderProgramMenu(g2d, menuState.getProgramMenu());
				break;
			case PROGRAM_SUB:
				menuState.getProgramMenuDescription().ifPresent(desc -> {
					renderString(g2d, desc, 1, 2, GAME_NAME);
				});
				renderProgramSubMenu(g2d, menuState.getProgramMenu());
				break;
			default:
				break;
		}
	}

	protected void renderProgramMenu(@Nonnull Graphics2D g2d, @Nonnull Menu menu) {
		for (int i = 0; i < menu.getItemCount(); i++) {
			renderString(g2d, menu.getMenuItem(i), 8, 12 + i, //
				menu.isSelected(i) ? INTENSE : SHORTCUT, //
				menu.isSelected(i) ? INTENSE : NORMAL);
		}
	}

	protected void renderProgramSubMenu(@Nonnull Graphics2D g2d, @Nonnull Menu menu) {
		for (int i = 0; i < menu.getItemCount(); i++) {
			renderString(g2d, menu.getMenuItem(i), 3, 3 + i, //
				menu.isSelected(i) ? INTENSE : SHORTCUT, //
				menu.isSelected(i) ? INTENSE : NORMAL);
		}
	}

}
