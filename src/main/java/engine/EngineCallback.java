package engine;

import engine.opcodes.EclString;

public interface EngineCallback {
	void showPicture(int id);

	void showText(EclString str);

	void loadArea(int id1, int id2, int id3);

	void loadAreaDecoration(int id1, int id2, int id3);
}
