package ui.shared.resource;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DungeonMapResource {
	private int[][] map;
	private Optional<DungeonResource> res;
	private Optional<int[][]> sqaureInfos;

	public DungeonMapResource(int[][] map) {
		this(map, null, null);
	}

	public DungeonMapResource(int[][] map, @Nullable DungeonResource res) {
		this(map, res, null);
	}

	public DungeonMapResource(int[][] map, @Nullable DungeonResource res, @Nullable int[][] squareInfos) {
		this.map = map;
		this.res = Optional.ofNullable(res);
		this.sqaureInfos = Optional.ofNullable(squareInfos);
	}

	@Nonnull
	public int[][] getMap() {
		return map;
	}

	public int getMapWidth() {
		return map[0].length;
	}

	public int getMapHeight() {
		return map.length;
	}

	@Nonnull
	public Optional<DungeonResource> getRes() {
		return res;
	}

	@Nonnull
	public Optional<int[][]> getSqaureInfos() {
		return sqaureInfos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(map);
		result = prime * result + Objects.hash(res);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DungeonMapResource)) {
			return false;
		}
		DungeonMapResource other = (DungeonMapResource) obj;
		return Arrays.deepEquals(map, other.map) && Objects.equals(res, other.res);
	}

	@Override
	public String toString() {
		return String.format("%s[%dx%d]", res.map(DungeonResource::toString).orElse(""), getMapWidth(), getMapHeight());
	}
}
