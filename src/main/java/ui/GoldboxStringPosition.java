package ui;

import javax.annotation.Nonnull;

import data.content.DungeonMap.Direction;
import engine.ViewDungeonPosition;
import types.CustomGoldboxString;

public class GoldboxStringPosition extends CustomGoldboxString {

	private ViewDungeonPosition position;
	private int x;
	private int y;
	private Direction dir;

	public GoldboxStringPosition(@Nonnull ViewDungeonPosition position) {
		super(createPositionString(position));
		this.position = position;

		this.dir = position.getDungeonDir();

		if (position.getExtendedDungeonX() != 255) {
			this.x = position.getExtendedDungeonX();
			this.y = position.getExtendedDungeonY();
		} else {
			this.x = position.getDungeonX();
			this.y = position.getDungeonY();
		}
	}

	@Override
	public byte getChar(int index) {
		if (index == 0) {
			Direction newDir = position.getDungeonDir();

			int newX, newY;
			if (position.getExtendedDungeonX() != 255) {
				newX = position.getExtendedDungeonX();
				newY = position.getExtendedDungeonY();
			} else {
				newX = position.getDungeonX();
				newY = position.getDungeonY();
			}
			if (dir != newDir || x != newX || y != newY) {
				setText(createPositionString(position));
				dir = newDir;
				x = newX;
				y = newY;
			}
		}
		return super.getChar(index);
	}

	private static String createPositionString(@Nonnull ViewDungeonPosition position) {
		if (position.getExtendedDungeonX() != 255) {
			return position.getExtendedDungeonX() + "," + position.getExtendedDungeonY() + " " + position.getDungeonDir().name().charAt(0);
		}
		return position.getDungeonX() + "," + position.getDungeonY() + " " + position.getDungeonDir().name().charAt(0);
	}
}
