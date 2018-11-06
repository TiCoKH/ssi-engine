package engine;

import java.util.List;

import engine.opcodes.EclString;

public interface EngineCallback {
	void clear();

	void clearPics();

	void setInput(InputType inputType);

	void setInputNumber(int maxDigits);

	void setInputString(int maxLetters);

	void setMenu(List<InputAction> action);

	void advanceSprite();

	void clearSprite();

	void showSprite(int spriteId, int index, int picId);

	void showPicture(int id);

	void showPicture(int gameState, int id);

	void addText(EclString str, boolean clear);

	void addNewline();

	void loadEcl(int id);

	void loadArea(int id1, int id2, int id3);

	void loadAreaDecoration(int id1, int id2, int id3);

	void updatePosition();

	void delayCurrentThread();

	public enum InputType {
		NONE, TITLE, CONTINUE, STANDARD;
	}
}
