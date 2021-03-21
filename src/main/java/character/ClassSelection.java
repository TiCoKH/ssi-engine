package character;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import io.vavr.collection.Array;
import io.vavr.collection.Seq;

import shared.CustomGoldboxString;
import shared.GoldboxString;

public class ClassSelection {
	private Seq<CharacterClass> classes;

	private ClassSelection(@Nonnull CharacterClass charClass) {
		this.classes = Array.of(charClass);
	}

	private ClassSelection(@Nonnull CharacterClass... classes) {
		this.classes = Array.of(classes);
	}

	public static ClassSelection of(@Nonnull CharacterClass charClass) {
		return new ClassSelection(charClass);
	}

	public static ClassSelection of(@Nonnull CharacterClass... classes) {
		return new ClassSelection(classes);
	}

	public int getClassesCount() {
		return classes.length();
	}

	public boolean contains(@Nonnull CharacterClass charClass) {
		return classes.contains(charClass);
	}

	public <U> Seq<U> map(Function<? super CharacterClass, ? extends U> mapper) {
		return classes.map(mapper);
	}

	public <T extends CharacterClass> void forEach(@Nonnull Class<T> clazz, @Nonnull Consumer<T> forEachConsumer) {
		classes //
			.filter(clazz::isInstance) //
			.map(clazz::cast) //
			.forEach(forEachConsumer::accept);
	}

	@SuppressWarnings("unchecked")
	public <T extends CharacterClass> T getClass(@Nonnull Class<T> clazz, int index) {
		if (clazz.isInstance(classes.get(index))) {
			return (T) classes.get(index);
		}
		return null;
	}

	public GoldboxString getDescription() {
		return classes //
			.map(CharacterClass::getName) //
			.map(GoldboxString::toString) //
			.reduceLeftOption((a, b) -> a + "/" + b) //
			.map(CustomGoldboxString::new) //
			.getOrElseThrow(IllegalStateException::new);
	}

	@Override
	public int hashCode() {
		return classes.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ClassSelection)) {
			return false;
		}
		ClassSelection other = (ClassSelection) obj;
		return Objects.deepEquals(classes, other.classes);
	}
}
