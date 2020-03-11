package engine.character;

import static data.ContentType.MONCHA;

import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.annotation.Nonnull;

import character.CharacterAlignment;
import character.CharacterGender;
import character.CharacterRace;
import character.ClassSelection;
import common.ByteBufferWrapper;
import data.ContentType;
import data.ResourceLoader;
import data.character.AbstractCharacter;
import data.character.CharacterBuckRogers;
import data.character.CharacterForgottenRealms;
import data.character.CharacterForgottenRealmsUnlimited;
import engine.EngineConfiguration;

public class PlayerDataFactory {
	private static final String UNKNOWN_CHARACTER_FORMAT = "unknown character format: ";

	private ResourceLoader res;
	private EngineConfiguration cfg;

	public PlayerDataFactory(@Nonnull ResourceLoader res, @Nonnull EngineConfiguration cfg) {
		this.res = res;
		this.cfg = cfg;
	}

	public AbstractCharacter createCharacter(@Nonnull CharacterRace race, @Nonnull CharacterGender gender, @Nonnull ClassSelection classes) {
		switch (cfg.getCharacterFormat()) {
			case BUCK_ROGERS:
				return new CharacterBuckRogers(race, gender, classes);
			default:
				throw new IllegalArgumentException(UNKNOWN_CHARACTER_FORMAT + cfg.getCharacterFormat());
		}
	}

	public AbstractCharacter createCharacter(@Nonnull CharacterRace race, @Nonnull CharacterGender gender, @Nonnull ClassSelection classes,
		@Nonnull CharacterAlignment alignment) {
		switch (cfg.getCharacterFormat()) {
			case FORGOTTEN_REALMS:
				return new CharacterForgottenRealms(race, gender, classes, alignment);
			case FORGOTTEN_REALMS_UNLIMITED:
				return new CharacterForgottenRealmsUnlimited(race, gender, classes, alignment);
			default:
				throw new IllegalArgumentException(UNKNOWN_CHARACTER_FORMAT + cfg.getCharacterFormat());
		}
	}

	public AbstractCharacter loadCharacter(@Nonnull FileChannel fc) throws IOException {
		final ByteBufferWrapper data = ByteBufferWrapper.allocateLE((int) fc.size());
		try {
			data.readFrom(fc);
		} finally {
			fc.close();
		}
		switch (cfg.getCharacterFormat()) {
			case BUCK_ROGERS:
				return new CharacterBuckRogers(data, ContentType.MONCHA);
			case FORGOTTEN_REALMS:
				return new CharacterForgottenRealms(data, ContentType.MONCHA);
			case FORGOTTEN_REALMS_UNLIMITED:
				return new CharacterForgottenRealmsUnlimited(data, ContentType.MONCHA);
			default:
				throw new IllegalArgumentException(UNKNOWN_CHARACTER_FORMAT + cfg.getCharacterFormat());
		}
	}

	public AbstractCharacter loadCharacter(int id) throws IOException {
		switch (cfg.getCharacterFormat()) {
			case BUCK_ROGERS:
				return res.find(id, CharacterBuckRogers.class, MONCHA);
			case FORGOTTEN_REALMS:
				return res.find(id, CharacterForgottenRealms.class, MONCHA);
			case FORGOTTEN_REALMS_UNLIMITED:
				return res.find(id, CharacterForgottenRealmsUnlimited.class, MONCHA);
			default:
				throw new IllegalArgumentException(UNKNOWN_CHARACTER_FORMAT + cfg.getCharacterFormat());
		}
	}
}
