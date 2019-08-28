package engine.text;

import static shared.GoldboxStringPart.PartType.LEADING_SPACE;
import static shared.GoldboxStringPart.PartType.SPACE;

import shared.GoldboxString;
import shared.GoldboxStringPart;

public class Space extends GoldboxStringPart {
	Space(GoldboxString source, int fromIndex, int toIndex) {
		super(fromIndex == 0 ? LEADING_SPACE : SPACE, source, fromIndex, toIndex);
	}

}
