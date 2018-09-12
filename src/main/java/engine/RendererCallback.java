package engine;

import data.content.DungeonMap.Direction;
import data.content.WallDef.WallPlacement;
import data.content.WallDef.WallDistance;

public interface RendererCallback {
	void textDisplayFinished();

	int getCurrentMapX();

	int getCurrentMapY();

	Direction getCurrentMapOrient();

	int[][] getWallDisplay(WallDistance dis, WallPlacement plc);
}
