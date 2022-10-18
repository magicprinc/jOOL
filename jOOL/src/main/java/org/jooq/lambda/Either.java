package org.jooq.lambda;

import org.jooq.lambda.tuple.Tuple2;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Mix of {@link Tuple2}, {@link Optional} and {@link Callable}
 * to represent (potentially failed to acquire) "optional" result in functional (Stream API) chain.
 * <br>
 * {@link #v1} - nullable returned value<br>
 * {@link #v2} - {@link Throwable} in case of failure (null otherwise)
 *
 * @param <T> the type of value
 *
 * @author Andrej Fink
 */
public class Either<T> extends Tuple2<T,Throwable> implements Callable<T> {

  private static final Either<?> EMPTY = new Either<>(null,null);

  public static <T> Either<T> empty () {
    return Wrap.castUnsafe(EMPTY);
  }


  /** Return this Either as {@link Optional} */
  public Optional<T> optValue () {
    return Optional.ofNullable(v1);
  }

  public Optional<Throwable> optThrowable () {
    return Optional.ofNullable(v2);
  }

  /*@Nullable*/ public Object either () {
    return v2 != null ? v2 : v1;
  }

  // Callable

  @Override @SuppressWarnings("RedundantThrows")
  /*@Nullable*/ public T call () throws Exception {
    if (v2 != null) {
      SeqUtils.sneakyThrow(v2);// throws Exception or Error!
    }
    return v1;
  }//Callable.call


  // creation

  private Either (T nullableValue, Throwable nullableThrowable) {
    super(nullableValue, nullableThrowable);
  }//new


  public static <T> Either<T> success (/*@Nullable*/ T value) {
    return new Either<>(value, null);
  }

  public static <T> Either<T> failure (Throwable t) {
    return new Either<>(null, Objects.requireNonNull(t));
  }


  public boolean isSuccess () {
    return v2 == null;
  }

  public boolean isFailure () {
    return v2 != null;
  }

  /**
   * If a value is present (success and not empty), returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public boolean isPresent() {
    return isSuccess() && v1 != null;
  }

  /**
   * If a value is not present (failure or null), returns {@code true}, otherwise {@code false}.
   *
   * @return  {@code true} if a value is not present, otherwise {@code false}
   */
  public boolean isEmpty() {
    return isFailure() || v1 == null;
  }

  /** Success, but null */
  public boolean isNull () {
    return isSuccess() && v1 == null;
  }


  public Either<T> throwIfFailure () throws IllegalStateException {
    if (isFailure()) {
      throw new IllegalStateException("Throwable instead of value", v2);
    }
    return this;
  }

  public Either<T> throwIfEmpty () throws IllegalStateException, NoSuchElementException {
    if (isFailure()) {
      throw new IllegalStateException("Throwable instead of value", v2);
    } else if (v1 == null) {
      throw new NoSuchElementException("No value present");
    }
    return this;
  }

  public Either<T> throwIfNull () throws NoSuchElementException {
    if (isNull()) {
      throw new NoSuchElementException("No value present");
    }
    return this;
  }


  public Either<T> ifPresent (Consumer</*@Nullable*/ ? super T> action) {
    if (isPresent()) {
      action.accept(v1);
    }
    return this;
  }


  public Either<T> ifNull (Runnable nullAction) {
    if (isNull()) {
      nullAction.run();
    }
    return this;
  }

  public Either<T> ifEmpty (Consumer</*@Nullable*/ Throwable> failureOrNullAction) {
    if (isEmpty()) {
      failureOrNullAction.accept(v2);
    }
    return this;
  }

  public Either<T> ifSuccess (Consumer</*@Nullable*/ ? super T> nullSafeAction) {
    if (isSuccess()) {
      nullSafeAction.accept(v1);
    }
    return this;
  }


  public Either<T> ifFailure (Consumer<Throwable> failureAction) {
    if (isFailure()) {
      failureAction.accept(v2);
    }
    return this;
  }


  public void consume (BiConsumer</*@Nullable*/ ? super T,Throwable> consumer) {
    consumer.accept(v1, v2);
  }


  /**
   * If a value is present, returns a sequential {@link Stream} containing
   * only that value, otherwise returns an empty {@code Stream}.
   * <p>
   * This method can be used to transform a {@code Stream} of optional
   * elements to a {@code Stream} of present value elements:
   * <pre>{@code
   *     Stream<Optional<T>> os = ..
   *     Stream<T> s = os.flatMap(Optional::stream)
   * }</pre>
   *
   * @return the optional value as a {@code Stream}
   */
  public Stream<T> stream () {
    if (isEmpty()) {
      return Stream.empty();
    } else {
      return Stream.of(v1);
    }
  }


  @Override
  public boolean equals (Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof Tuple2<?,?>) {
      Tuple2<?,?> o = (Tuple2<?,?>) obj;
      return Objects.equals(v1, o.v1) && Objects.equals(v2, o.v2);

    } else if (isSuccess() && obj instanceof Optional<?>) {
      Optional<?> o = (Optional<?>) obj;
      return Objects.equals(v1, o) || Objects.equals(v1, o.orElse(null));

    } else if (isSuccess()) {
      return Objects.equals(v1, obj);

    } else if (isFailure() && obj instanceof Throwable) {
      return Objects.equals(v2, obj);
    }
    return false;
  }


  /**
   * Returns a non-empty string representation of this {@code Either}
   * suitable for debugging.
   *
   * @return the string representation of this instance
   */
  @Override public String toString () {
    if (isFailure()) {
      return "Either.Failure("+ v2+')';

    } else if (isPresent()) {
      return "Either("+ v1 +')';
    }
    return "Either.Empty";
  }
}