package shared;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.vavr.collection.Seq;

import data.ContentType;
import data.dungeon.DungeonMap.VisibleWalls;
import shared.party.CharacterSheet;

public interface UserInterface {

	void resize();

	void start(boolean showTitles);

	void stop();

	void clear();

	void clearAll();

	void addText(boolean withClear, Seq<GoldboxStringPart> text);

	void addRunicText(GoldboxStringPart text);

	void showPicture(int headId, int bodyId);

	void showPicture(int id, ContentType type);

	void clearPictures();

	void showSprite(int spriteId, int headId, int bodyId, int distance);

	void showSprite(int spriteId, int picId, int distance);

	void advanceSprite();

	void clearSprite();

	void clearStatus();

	void setStatus(GoldboxString status);

	void clearCurrentDialog();

	void clearAllDialogs();

	void showProgramMenuDialog(@Nonnull ProgramMenuType programType, @Nonnull Seq<InputAction> programMenu,
		@Nonnull Seq<InputAction> horizontalMenu, @Nullable GoldboxString description, @Nonnull InputAction menuSelect);

	void showCharacterSheet(CharacterSheet sheet, @Nonnull Seq<InputAction> horizontalMenu);

	void showCharacterSheet(CharacterSheet sheet, @Nonnull Seq<InputAction> horizontalMenu,
		@Nullable GoldboxString description);

	void setNoResources();

	void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nullable VisibleWalls visibleWalls,
		@Nullable int[][] map, int[] decoIds);

	void setOverlandResources(ViewOverlandPosition position, int mapId);

	void setSpaceResources(ViewSpacePosition position);

	void setGlobalData(ViewGlobalData globalData);

	void setInputNone();

	void setInputMenu(MenuType type, Seq<InputAction> menuItems, GoldboxString description, InputAction selected);

	void setInputNumber(int maxDigits);

	void setInputString(int maxLetters);

	void switchDungeonAreaMap();

	void setPortraitFrameVisible(boolean enabled);
}
