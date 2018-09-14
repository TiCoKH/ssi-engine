package engine;

import data.content.WallDef.WallDistance;
import data.content.WallDef.WallPlacement;
import engine.opcodes.EclString;

public interface RendererCallback {
	void textDisplayFinished();

	EclString getPositionText();;

	int getBackdropIndex();

	int[][] getWallDisplay(WallDistance dis, WallPlacement plc);
}
