package data.character;

import static data.HLIBContent.uncompress;

import javax.annotation.Nonnull;

import character.CharacterAlignment;
import character.CharacterGender;
import character.CharacterRace;
import character.ClassSelection;
import common.ByteBufferWrapper;
import data.ContentType;

public class CharacterForgottenRealmsUnlimited extends CharacterForgottenRealms {

	public CharacterForgottenRealmsUnlimited(@Nonnull ByteBufferWrapper data, @Nonnull ContentType type) {
		super(uncompress(data.position(2).slice(), CHAR_VALUES.getFileSize()), type);
	}

	public CharacterForgottenRealmsUnlimited(@Nonnull CharacterRace race, @Nonnull CharacterGender gender, @Nonnull ClassSelection classSelection,
		@Nonnull CharacterAlignment alignment) {

		super(race, gender, classSelection, alignment);
	}
}
