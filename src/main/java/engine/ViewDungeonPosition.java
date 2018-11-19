package engine;

import data.content.DungeonMap.Direction;

public interface ViewDungeonPosition {
	int getExtendedDungeonX();

	int getExtendedDungeonY();

	int getDungeonX();

	int getDungeonY();

	Direction getDungeonDir();

	int getBackdropIndex();
}
