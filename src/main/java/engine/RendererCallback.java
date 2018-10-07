package engine;

import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;

public interface RendererCallback {
	void textDisplayFinished();

	void handleInput(InputAction action);

	int getBackdropIndex();

	int[][] getWallDisplay(WallDistance dis, WallPlacement plc);
}
