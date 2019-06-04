package types;

import java.util.List;

import data.content.DAXContentType;
import data.content.DungeonMap.VisibleWalls;
import engine.InputAction;
import engine.ViewDungeonPosition;
import engine.ViewOverlandPosition;
import engine.ViewSpacePosition;

public interface UserInterface {

	void resize();

	void start(boolean showTitles);

	void stop();

	void switchUIState(UIState title);

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

	void setDungeonResources(ViewDungeonPosition position, VisibleWalls visibleWalls, int decoId1, int decoId2, int decoId3);

	void setOverlandResources(ViewOverlandPosition position, int mapId);

	void setSpaceResources(ViewSpacePosition position);

	void setInputNone();

	void setInputStandard();

	void setInputMenu(MenuType type, List<InputAction> menuItems, GoldboxString description);

	void setInputNumber(int maxDigits);

	void setInputString(int maxLetters);

	public enum UIState {
		TITLE, STORY, BIGPIC, DUNGEON, OVERLAND, SPACE
	}
}
