package ui.shared.text;

import javax.annotation.Nonnull;

import data.dungeon.DungeonMap.Direction;
import shared.CustomGoldboxString;
import shared.ViewDungeonPosition;

public class GoldboxStringPosition extends CustomGoldboxString {

	private ViewDungeonPosition position;
	private int x;
	private int y;
	private Direction dir;
	private boolean searchActive;

	public GoldboxStringPosition(@Nonnull ViewDungeonPosition position) {
		super(createPositionString(position));
		this.position = position;

		this.dir = position.getDungeonDir();
		this.searchActive = position.getSearchFlagsIsSearchActive();

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
			boolean newSearchActive = position.getSearchFlagsIsSearchActive();

			int newX, newY;
			if (position.getExtendedDungeonX() != 255) {
				newX = position.getExtendedDungeonX();
				newY = position.getExtendedDungeonY();
			} else {
				newX = position.getDungeonX();
				newY = position.getDungeonY();
			}
			if (dir != newDir || x != newX || y != newY || searchActive != newSearchActive) {
				setText(createPositionString(position));
				dir = newDir;
				searchActive = newSearchActive;
				x = newX;
				y = newY;
			}
		}
		return super.getChar(index);
	}

	private static String createPositionString(@Nonnull ViewDungeonPosition position) {
		StringBuilder sb = new StringBuilder();
		if (position.getExtendedDungeonX() != 255) {
			sb.append(position.getExtendedDungeonX());
			sb.append(",");
			sb.append(position.getExtendedDungeonY());
		} else {
			sb.append(position.getDungeonX());
			sb.append(",");
			sb.append(position.getDungeonY());
		}
		sb.append(" ");
		sb.append(position.getDungeonDir().name().charAt(0));
		if (position.getSearchFlagsIsSearchActive()) {
			sb.append(" SEARCH");
		}
		return sb.toString();
	}
}
