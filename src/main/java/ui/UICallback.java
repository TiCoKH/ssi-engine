package ui;

import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import engine.InputAction;

public interface UICallback {
	void textDisplayFinished();

	void handleInput(InputAction action);

	int getBackdropIndex();

	int[][] getWallDisplay(WallDistance dis, WallPlacement plc);
}
