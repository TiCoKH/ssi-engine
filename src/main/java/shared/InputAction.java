package shared;

public interface InputAction {
	static final GoldboxString LOAD = new CustomGoldboxString("");
	static final GoldboxString SAVE = new CustomGoldboxString("");
	static final GoldboxString FORWARD_UP = new CustomGoldboxString("");
	static final GoldboxString TURN_LEFT = new CustomGoldboxString("");
	static final GoldboxString TURN_RIGHT = new CustomGoldboxString("");
	static final GoldboxString UTURN_DOWN = new CustomGoldboxString("");

	GoldboxString getName();
}
