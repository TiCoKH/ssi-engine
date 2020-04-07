package shared;

import data.dungeon.DungeonMap.Direction;

public interface ViewOverlandPosition {

	Direction getOverlandDir();

	int getOverlandX();

	int getOverlandY();
}
