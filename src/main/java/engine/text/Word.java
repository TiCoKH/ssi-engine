package engine.text;

import static shared.GoldboxStringPart.PartType.TEXT;

import shared.GoldboxString;
import shared.GoldboxStringPart;

public class Word extends GoldboxStringPart {
	Word(GoldboxString source, int fromIndex, int toIndex) {
		super(TEXT, source, fromIndex, toIndex);
	}
}
