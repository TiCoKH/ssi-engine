package ui.classic;

import static shared.FontColor.NORMAL;
import static shared.FontColor.PC_HEADING;
import static ui.shared.UIFrame.SHEET;

import java.awt.Graphics2D;

import javax.annotation.Nonnull;

import shared.CustomGoldboxString;
import shared.GameFeature;
import shared.party.CharacterSheet;
import shared.party.CharacterValue;
import ui.UISettings;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;

public class CharacterSheetRenderer extends AbstractDialogRenderer {

	private final UIResourceConfiguration config;

	public CharacterSheetRenderer(@Nonnull UIResourceConfiguration config, @Nonnull UISettings settings, @Nonnull UIResourceManager resman,
		@Nonnull AbstractFrameRenderer frameRenderer) {

		super(settings, resman, frameRenderer);
		this.config = config;
	}

	@Override
	public void render(@Nonnull Graphics2D g2d, @Nonnull AbstractDialogState state) {
		final CharacterSheetState menuState = (CharacterSheetState) state;
		renderFrame(g2d, SHEET);
		renderCharSheet(g2d, menuState);
		renderHorizontalMenu(g2d, menuState.getHorizontalMenu());
	}

	protected void renderCharSheet(@Nonnull Graphics2D g2d, CharacterSheetState menuState) {
		if (config.isUsingFeature(GameFeature.SPACE_TRAVEL)) {
			renderCharSheetSpace(g2d, menuState.getSheet());
		} else {
			renderCharSheetFantasy(g2d, menuState.getSheet());
		}
	}

	protected void renderCharSheetFantasy(@Nonnull Graphics2D g2d, @Nonnull CharacterSheet sheet) {
		renderString(g2d, sheet.getName(), 1, 1, PC_HEADING);
		renderString(g2d, sheet.getGenderDescription(), 1, 3, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("AGE"), 12, 3, PC_HEADING);
		renderString(g2d, sheet.getAgeDescription(), 16, 3, PC_HEADING);
		sheet.withAlignment().ifPresent(alignment -> {
			renderString(g2d, alignment.getAlignmentDescription(), 1, 4, PC_HEADING);
		});
		renderString(g2d, sheet.getClassDescription(), 1, 5, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("LEVEL"), 1, 7, PC_HEADING);
		renderString(g2d, sheet.getLevelDescription(), 7, 7, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("STATUS:"), 20, 1, PC_HEADING);
		renderString(g2d, sheet.getStatusDescription(), 27, 1, NORMAL);
		renderString(g2d, new CustomGoldboxString("HIT POINTS"), 20, 3, PC_HEADING);
		renderString(g2d, sheet.getHPDescription(), 31, 3, NORMAL);
		renderString(g2d, sheet.getRaceDescription(), 20, 4, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("EXPERIENCE:"), 24, 6, PC_HEADING);
		renderNumber(g2d, sheet.getExperienceDescription(), 34, 7, PC_HEADING);

		int i = 0;
		for (CharacterValue v : sheet.getStatDescriptions()) {
			renderString(g2d, v.getName(), 1, 9 + i, NORMAL);
			renderString(g2d, v.getValue(), 5, 9 + i, NORMAL);
			i++;
		}
		i = 0;
		for (CharacterValue v : sheet.getMoneyDescriptions()) {
			renderString(g2d, v.getName(), 20, 9 + i, NORMAL);
			renderNumber(g2d, v.getValue(), 38, 9 + i, NORMAL);
			i++;
		}
		renderString(g2d, new CustomGoldboxString("ARMOR CLASS"), 1, 17, PC_HEADING);
		renderNumber(g2d, sheet.getArmorClassDescription(), 18, 17, NORMAL);
		renderString(g2d, new CustomGoldboxString("THACO"), 1, 18, PC_HEADING);
		renderNumber(g2d, sheet.getTHACODescription(), 18, 18, NORMAL);
		renderString(g2d, new CustomGoldboxString("DAMAGE"), 1, 19, PC_HEADING);
		renderNumber(g2d, sheet.getDamageDescription(), 18, 19, NORMAL);
		renderString(g2d, new CustomGoldboxString("ENCUMBRANCE"), 20, 17, PC_HEADING);
		renderNumber(g2d, sheet.getEncumbranceDescription(), 38, 17, NORMAL);
		renderString(g2d, new CustomGoldboxString("MOVEMENT"), 20, 18, PC_HEADING);
		renderNumber(g2d, sheet.getMovementRateDescription(), 38, 18, NORMAL);
	}

	protected void renderCharSheetSpace(@Nonnull Graphics2D g2d, @Nonnull CharacterSheet sheet) {
		renderString(g2d, new CustomGoldboxString("NAME  :"), 1, 1, NORMAL);
		renderString(g2d, sheet.getName(), 8, 1, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("RACE  :"), 1, 2, NORMAL);
		renderString(g2d, sheet.getRaceDescription(), 8, 2, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("GENDER:"), 1, 3, NORMAL);
		renderString(g2d, sheet.getGenderDescription(), 8, 3, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("CAREER:"), 1, 4, NORMAL);
		renderString(g2d, sheet.getClassDescription(), 8, 4, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("STATUS:"), 1, 6, NORMAL);
		renderString(g2d, sheet.getStatusDescription(), 8, 6, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("CREDIT:"), 1, 7, NORMAL);
		renderString(g2d, sheet.getMoneyDescriptions().get(0).getValue(), 8, 7, PC_HEADING);

		renderString(g2d, new CustomGoldboxString("MOVMNT:"), 1, 9, NORMAL);
		renderString(g2d, sheet.getMovementRateDescription(), 8, 9, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("DAMAGE:"), 1, 10, NORMAL);
		renderString(g2d, sheet.getDamageDescription(), 8, 10, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("WEAPON:"), 1, 11, NORMAL);
		renderString(g2d, sheet.getEquippedWeaponDescription(), 8, 11, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("ARMOR :"), 1, 12, NORMAL);
		renderString(g2d, sheet.getEquippedArmorDescription(), 8, 12, PC_HEADING);

		renderString(g2d, new CustomGoldboxString("ABILITIES:"), 1, 14, NORMAL);

		renderString(g2d, new CustomGoldboxString("HP:"), 26, 1, NORMAL);
		renderString(g2d, sheet.getHPDescription(), 29, 1, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("AC:"), 26, 2, NORMAL);
		renderString(g2d, sheet.getArmorClassDescription(), 29, 2, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("THACO:"), 23, 3, NORMAL);
		renderString(g2d, sheet.getTHACODescription(), 29, 3, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("LEVEL:"), 23, 4, NORMAL);
		renderString(g2d, sheet.getLevelDescription(), 29, 4, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("EXPERIENCE:"), 18, 6, NORMAL);
		renderString(g2d, sheet.getExperienceDescription(), 29, 6, PC_HEADING);
		renderString(g2d, new CustomGoldboxString("AGE:"), 25, 7, NORMAL);
		renderString(g2d, sheet.getAgeDescription(), 29, 7, PC_HEADING);

		renderString(g2d, new CustomGoldboxString("ENCUMBRANCE:"), 19, 9, NORMAL);
		renderString(g2d, sheet.getEncumbranceDescription(), 31, 9, PC_HEADING);

		renderString(g2d, new CustomGoldboxString("CAREER SKILLS:"), 12, 14, NORMAL);

		int i = 0;
		for (CharacterValue v : sheet.getStatDescriptions()) {
			renderString(g2d, v.getName(), 1, 15 + i, NORMAL);
			renderString(g2d, v.getValue(), 5, 15 + i, NORMAL);
			i++;
		}

		renderString(g2d, sheet.getName(), 8, 1, PC_HEADING);
	}
}
