package shared;

import data.dungeon.DungeonMap.Direction;

public interface ViewDungeonPosition {
	int getSkyColorOutdoors();

	int getSkyColorIndoors();

	int getExtendedDungeonX();

	int getExtendedDungeonY();

	int getDungeonX();

	int getDungeonY();

	Direction getDungeonDir();

	int getBackdropIndex();

	boolean getSearchFlagsIsSearchActive();
}
