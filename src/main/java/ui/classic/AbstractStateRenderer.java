package ui.classic;

import static shared.FontColor.INTENSE;
import static shared.FontColor.NORMAL;
import static shared.FontColor.SHORTCUT;
import static shared.GoldboxStringPart.PartType.COLOR;
import static shared.GoldboxStringPart.PartType.LINE_BREAK;
import static shared.GoldboxStringPart.PartType.SPACE;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import shared.FontColor;
import shared.GoldboxStringPart;
import shared.MenuType;
import ui.UISettings;
import ui.shared.Menu;
import ui.shared.resource.UIResourceManager;
import ui.shared.text.StoryText;

public abstract class AbstractStateRenderer extends AbstractRenderer {

	protected RendererState state;

	protected AbstractStateRenderer(@Nonnull RendererState state, @Nonnull UISettings settings, @Nonnull UIResourceManager resman,
		@Nonnull AbstractFrameRenderer frameRenderer) {

		super(settings, resman, frameRenderer);
		this.state = state;
	}

	public abstract int getLineWidth();

	public abstract int getTextStartX();

	public abstract int getTextStartY();

	public abstract void render(@Nonnull Graphics2D g2d);

	protected void renderText(@Nonnull Graphics2D g2d) {
		StoryText st = state.getStoryText();
		st.getTextList().ifPresent(
			text -> renderText(g2d, text, getTextStartX(), getTextStartY(), getLineWidth(), st.getCharStop(), st.getDefaultTextColor(), 0));
	}

	protected int renderText(@Nonnull Graphics2D g2d, List<GoldboxStringPart> text, int startX, int StartY, int lineLength, int charStop,
		Optional<FontColor> initialColor, int initialOffset) {

		int line = 0;
		int pos = initialOffset;
		int index = 0;
		int renderCount = 0;
		Optional<FontColor> fc = initialColor;
		while (index < text.size() && (charStop == -1 || renderCount < charStop)) {
			GoldboxStringPart tp = text.get(index++);
			if (COLOR.equals(tp.getType())) {
				fc = tp.getFontColor();
				continue;
			}
			if (LINE_BREAK.equals(tp.getType()) || tp.getLength() > lineLength - pos) {
				line++;
				pos = 0;
			}
			if (SPACE.equals(tp.getType()) && pos == 0) {
				continue;
			}
			int i = 0;
			while ((charStop == -1 || renderCount < charStop) && i < tp.getLength()) {
				int x = startX + pos;
				int y = StartY + line;
				int c = tp.getChar(i++);
				FontColor fontColor = tp.getFontColor().orElse(fc.orElse(NORMAL));
				if (c < 0) {
					c += resman.getFont(fontColor).size();
				}
				renderChar(g2d, x, y, c, fontColor);
				renderCount++;
				pos++;
			}
		}
		return line;
	}

	protected void renderStatus(@Nonnull Graphics2D g2d) {
		state.getStatusLine().ifPresent(status -> {
			renderString(g2d, status, 0, 24, NORMAL);
		});
	}

	protected void renderMenu(@Nonnull Graphics2D g2d) {
		state.getMenu().ifPresent(menu -> {
			switch (menu.getType()) {
				case HORIZONTAL:
					renderHorizontalMenu(g2d, menu);
					break;
				case VERTICAL:
					renderVerticalMenu(g2d, menu);
					break;
				case PROGRAM:
					break;
				case PROGRAM_SUB:
					break;
				default:
					break;
			}
		});
	}

	protected void renderVerticalMenu(@Nonnull Graphics2D g2d, @Nonnull Menu menu) {
		menu.getDescription().ifPresent(desc -> {
			renderString(g2d, desc, 1, 17, NORMAL);
		});
		int firstLine = menu.getDescription().map(desc -> 18).orElse(17);
		for (int i = 0; i < menu.getItemCount(); i++) {
			renderString(g2d, menu.getMenuItem(i), 1, firstLine + i, //
				menu.isSelected(i) ? INTENSE : SHORTCUT, //
				menu.isSelected(i) ? INTENSE : NORMAL);
		}
	}

	protected void renderMenuOrTextStatus(@Nonnull Graphics2D g2d) {
		if (state.getMenu().filter(menu -> menu.getType() == MenuType.VERTICAL).isPresent())
			renderMenu(g2d);
		else
			renderText(g2d);
		if (state.getMenu().filter(menu -> menu.getType() == MenuType.HORIZONTAL).isPresent())
			renderMenu(g2d);
		else
			renderStatus(g2d);
	}
}
