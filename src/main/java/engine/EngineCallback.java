package engine;

import java.util.List;

import engine.opcodes.EclString;

public interface EngineCallback {
	void setInput(InputType inputType);

	void setMenu(List<InputAction> action);

	void showPicture(int id);

	void addText(EclString str, boolean clear);

	void addNewline();

	void loadEcl(int id);

	void loadArea(int id1, int id2, int id3);

	void loadAreaDecoration(int id1, int id2, int id3);

	public enum InputType {
		NONE, TITLE, CONTINUE, STANDARD;
	}
}
