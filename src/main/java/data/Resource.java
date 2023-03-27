package data;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import io.vavr.control.Option;
import io.vavr.control.Try;

public class Resource<T> {

	private final Optional<Try<T>> res;

	private Resource(@Nonnull Optional<Try<T>> res) {
		this.res = res;
	}

	public T get() {
		return res.map(Try::get).orElseThrow();
	}

	public T getOrElse(Supplier<T> supplier) {
		return res.map(Try::get).orElseGet(supplier);
	}

	@SuppressWarnings("unchecked")
	public <R> Resource<R> flatMap(@Nonnull Function<T, Resource<R>> mapper) {
		if (isPresentAndSuccess()) {
			final T t = res.get().get();
			return mapper.apply(t);
		}
		return (Resource<R>) this;
	}

	public Resource<T> ifPresent(@Nonnull Consumer<Try<T>> consumer) {
		if (isPresent()) {
			final Try<T> t = res.get();
			consumer.accept(t);
		}
		return this;
	}

	public Resource<T> ifPresentAndSuccess(@Nonnull Consumer<T> consumer) {
		if (isPresentAndSuccess()) {
			final T t = res.get().get();
			consumer.accept(t);
		}
		return this;
	}

	public Resource<T> ifFailure(@Nonnull Consumer<Throwable> consumer) {
		if (isFailure()) {
			final Throwable t = res.get().getCause();
			consumer.accept(t);
		}
		return this;
	}

	public boolean isPresent() {
		return res.isPresent();
	}

	public boolean isPresentAndSuccess() {
		return res.map(Try::isSuccess).orElse(false);
	}

	public boolean isFailure() {
		return res.map(Try::isFailure).orElse(false);
	}

	@SuppressWarnings("unchecked")
	public <R> Resource<R> map(@Nonnull Function<T, R> mapper) {
		if (isPresentAndSuccess()) {
			final T t = res.get().get();
			return Resource.<R>of(mapper.apply(t));
		}
		return (Resource<R>) this;
	}

	public static <T> Resource<T> empty() {
		return new Resource<>(Optional.empty());
	}

	public static <T> Resource<T> of(@Nonnull T res) {
		return new Resource<>(Optional.of(Try.success(res)));
	}

	public static <T> Resource<T> of(@Nonnull Throwable t) {
		return new Resource<>(Optional.of(Try.failure(t)));
	}

	public static <T> Resource<T> of(@Nonnull Try<T> res) {
		return new Resource<>(Optional.of(res));
	}

	public static <T> Resource<T> of(@Nonnull Option<Try<T>> res) {
		return new Resource<>(res.toJavaOptional());
	}

	public static <T> Resource<T> of(@Nonnull Optional<Try<T>> res) {
		return new Resource<>(res);
	}

	public static <T> Resource<T> ofOptional(@Nonnull Optional<T> res) {
		return new Resource<>(res.map(Try::success));
	}
}
