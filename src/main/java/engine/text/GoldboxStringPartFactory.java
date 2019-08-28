package engine.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import shared.FontColor;
import shared.GoldboxString;
import shared.GoldboxStringPart;

public class GoldboxStringPartFactory {

	private Map<Character, SpecialCharType> charMap = new HashMap<>();

	public GoldboxStringPartFactory() {
	}

	public GoldboxStringPartFactory(char umlautAe, char umlautOe, char umlautUe, char sharpSz) {
		this.charMap.put(umlautAe, SpecialCharType.UMLAUT_A);
		this.charMap.put(umlautOe, SpecialCharType.UMLAUT_O);
		this.charMap.put(umlautUe, SpecialCharType.UMLAUT_U);
		this.charMap.put(sharpSz, SpecialCharType.SHARP_S);
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

	public List<GoldboxStringPart> from(@Nonnull GoldboxString s) {
		List<GoldboxStringPart> result = new ArrayList<>();
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
				SpecialChar sc = new SpecialChar(charMap.get(c));
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
							result.add(w);
					} else {
						result.add(w != null ? cmp.plus(w) : cmp);
						cmp = null;
					}
					result.add(new TextColor(s, from + 1, to));
				}
				from = to;
			} else if (c == ' ') {
				if (from != to) {
					Word w = new Word(s, from, to);
					if (cmp == null) {
						result.add(w);
					} else {
						result.add(cmp.plus(w));
						cmp = null;
					}
					from = to;
				}
				do {
					to++;
					c = to < s.getLength() ? s.getCharAsAscii(to) : '\0';
				} while (c == ' ');
				result.add(new Space(s, from, to));
				from = to;
			} else {
				to++;
			}
		}
		if (from != to) {
			Word w = new Word(s, from, to);
			if (cmp == null) {
				result.add(w);
			} else {
				result.add(cmp.plus(w));
			}
		}
		return result;
	}
}
