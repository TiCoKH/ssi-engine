package engine;

import java.util.List;

import engine.opcodes.EclString;

public interface EngineCallback {
	void setInputHandler(InputType inputType, String description, List<InputAction> action);

	void showPicture(int id);

	void addText(EclString str, boolean clear);

	void addNewline();

	void loadArea(int id1, int id2, int id3);

	void loadAreaDecoration(int id1, int id2, int id3);

	enum InputType {
		NONE, TITLE, RETURN, MENU, MOVEMENT;
	}
}
