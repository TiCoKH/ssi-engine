package ui;

import java.util.Objects;

public class DungeonResource {
	private int id1, id2, id3;

	public DungeonResource(int id1) {
		this(id1, 127, 127);
	}

	public DungeonResource(int id1, int id2, int id3) {
		this.id1 = id1;
		this.id2 = id2;
		this.id3 = id3;
	}

	public int getId1() {
		return id1;
	}

	public int getId2() {
		return id2;
	}

	public int getId3() {
		return id3;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id1, id2, id3);
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
		return id1 == other.id1 && id2 == other.id2 && id3 == other.id3;
	}

	@Override
	public String toString() {
		if (id2 == 127 && id3 == 127)
			return Integer.toString(id1);
		return Integer.toString(id1) + ", " + Integer.toString(id2) + ", " + Integer.toString(id3);
	}
}
