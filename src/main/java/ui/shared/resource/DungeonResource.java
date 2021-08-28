package ui.shared.resource;

import java.util.Arrays;

public class DungeonResource {
	private int[] ids;

	public DungeonResource(int id1) {
		this(new int[] { id1, 127, 127 });
	}

	public DungeonResource(int[] ids) {
		this.ids = ids;
	}

	public int[] getIds() {
		return ids;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(ids);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DungeonResource)) {
			return false;
		}
		DungeonResource other = (DungeonResource) obj;
		return Arrays.equals(ids, other.ids);
	}

	@Override
	public String toString() {
		if (ids[1] == 127 && ids[2] == 127)
			return Integer.toString(ids[0]);
		return Arrays.toString(ids);
	}
}
