package shared;

public interface ViewSpacePosition {
	int getCelestialX(Celestial c);

	int getCelestialY(Celestial c);

	int getFuel();

	int getSpaceX();

	int getSpaceY();

	SpaceDirection getSpaceDir();

	enum Celestial {
		MERKUR, VENUS, EARTH, MARS, CERES, VESTA, FORTUNA, PALLAS, PSYCHE, JUNO, HYGEIA, AURORA, THULE;
	}

	enum SpaceDirection {
		UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT;
	}
}
