package engine;

import engine.opcodes.EclString;

public interface EngineCallback {
	void showPicture(int id);

	void showText(EclString str);
}
