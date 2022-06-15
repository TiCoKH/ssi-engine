package engine.text;

import static io.vavr.API.Seq;

import javax.annotation.Nonnull;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;

import shared.FontColor;
import shared.GoldboxString;
import shared.GoldboxStringPart;

public class GoldboxStringPartFactory {

	private Map<Character, SpecialCharType> charMap;

	public GoldboxStringPartFactory() {
		charMap = HashMap.empty();
	}

	public GoldboxStringPartFactory(char umlautAe, char umlautOe, char umlautUe, char sharpSz) {
		charMap = HashMap.of( //
			umlautAe, SpecialCharType.UMLAUT_A, //
			umlautOe, SpecialCharType.UMLAUT_O, //
			umlautUe, SpecialCharType.UMLAUT_U, //
			sharpSz, SpecialCharType.SHARP_S //
		);
	}

	public GoldboxStringPart createLineBreak() {
		return new LineBreak();
	}

	public GoldboxStringPart fromTextColor(@Nonnull FontColor textColor) {
		return new TextColor(textColor);
	}

	public GoldboxStringPart fromTextColor(int textColor) {
		return new TextColor(textColor);
	}

	public GoldboxStringPart fromRunicText(@Nonnull GoldboxString s) {
		return new Runes(s, 0, s.getLength());
	}

	public GoldboxString fromMenu(@Nonnull GoldboxString s) {
		return new Composite(from(s));
	}

	public Seq<GoldboxStringPart> from(@Nonnull GoldboxString s) {
		Seq<GoldboxStringPart> result = Seq();
		int from = 0;
		int to = 0;
		Composite cmp = null;
		while (to < s.getLength()) {
			char c = s.getCharAsAscii(to);
			if (charMap.containsKey(c)) {
				if (from != to) {
					Word w = new Word(s, from, to);
					cmp = cmp == null ? new Composite(w) : cmp.plus(w);
				}
				final SpecialChar sc = new SpecialChar(charMap.get(c).get());
				cmp = cmp == null ? new Composite(sc) : cmp.plus(sc);
				from = to += 1;
			} else if (c == '%') {
				Word w = null;
				if (from != to) {
					w = new Word(s, from, to);
					from = to;
				}
				to += 2;
				c = s.getCharAsAscii(from + 1);
				if (c == '%') {
					cmp = cmp == null ? new Composite(w) : cmp.plus(w);
					cmp = cmp.plus(new Word(s, from + 1, to));
				} else {
					if (cmp == null) {
						if (w != null)
							result = result.append(w);
					} else {
						result = result.append(w != null ? cmp.plus(w) : cmp);
						cmp = null;
					}
					result = result.append(new TextColor(s, from + 1, to));
				}
				from = to;
			} else if (c == ' ') {
				if (from != to) {
					Word w = new Word(s, from, to);
					if (cmp == null) {
						result = result.append(w);
					} else {
						result = result.append(cmp.plus(w));
						cmp = null;
					}
					from = to;
				}
				do {
					to++;
					c = to < s.getLength() ? s.getCharAsAscii(to) : '\0';
				} while (c == ' ');
				result = result.append(new Space(s, from, to));
				from = to;
			} else {
				to++;
			}
		}
		if (from != to) {
			Word w = new Word(s, from, to);
			if (cmp == null) {
				result = result.append(w);
			} else {
				result = result.append(cmp.plus(w));
			}
		}
		return result;
	}
}
