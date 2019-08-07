package shared;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import data.content.DAXContentType;
import data.content.DungeonMap.VisibleWalls;

public interface UserInterface {

	void resize();

	void start(boolean showTitles);

	void stop();

	void clear();

	void clearAll();

	void addText(GoldboxString str);

	void addLineBreak();

	void clearText();

	void showPicture(int headId, int bodyId);

	void showPicture(int id, DAXContentType type);

	void clearPictures();

	void showSprite(int spriteId, int headId, int bodyId, int distance);

	void showSprite(int spriteId, int picId, int distance);

	void advanceSprite();

	void clearSprite();

	void clearStatus();

	void setStatus(GoldboxString status);

	void setNoResources();

	void setDungeonResources(@Nonnull ViewDungeonPosition position, @Nullable VisibleWalls visibleWalls, @Nullable int[][] map, int[] decoIds);

	void setOverlandResources(ViewOverlandPosition position, int mapId);

	void setSpaceResources(ViewSpacePosition position);

	void setInputNone();

	void setInputMenu(MenuType type, List<InputAction> menuItems, GoldboxString description, InputAction selected);

	void setInputNumber(int maxDigits);

	void setInputString(int maxLetters);

	void switchDungeonAreaMap();
}
