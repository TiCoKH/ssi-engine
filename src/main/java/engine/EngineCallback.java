package engine;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import types.GoldboxString;
import ui.Menu.MenuType;

public interface EngineCallback {
	void clear();

	void clearPics();

	void setInputNumber(int maxDigits);

	void setInputString(int maxLetters);

	void setMenu(@Nonnull MenuType type, @Nonnull List<InputAction> menuItems, @Nullable GoldboxString description);

	void advanceSprite();

	void clearSprite();

	void showSprite(int spriteId, int index, int picId);

	void showPicture(int id);

	void showPicture(int gameState, int id);

	void addText(GoldboxString str, boolean clear);

	void addNewline();

	void loadEcl(int id);

	void loadArea(int id1, int id2, int id3);

	void loadAreaDecoration(int id1, int id2, int id3);

	void updatePosition();

	void delayCurrentThread();
}
