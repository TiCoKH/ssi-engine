package engine;

import data.content.DungeonMap.Direction;

public interface ViewDungeonPosition {

	int getCurrentMapX();

	int getCurrentMapY();

	Direction getCurrentMapOrient();

	int getBackdropIndex();
}
