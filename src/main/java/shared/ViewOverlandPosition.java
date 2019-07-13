package shared;

import data.content.DungeonMap.Direction;

public interface ViewOverlandPosition {

	Direction getOverlandDir();

	int getOverlandX();

	int getOverlandY();
}
