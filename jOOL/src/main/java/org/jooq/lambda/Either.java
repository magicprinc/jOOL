package org.jooq.lambda;

import org.jooq.lambda.tuple.Tuple2;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Mix of {@link Optional}, {@link Callable}, {@link Tuple2} and {@link Future}
 * to represent (potentially failed to acquire) "optional" result.
 *
 * @param <T> the type of value
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Either<T> implements Callable<T>, Supplier<T> {

  private final Optional<T> value;
  private final Throwable throwable;


  /** Return this Either as {@link Optional} */
  public Optional<T> optional () {
    return value;
  }

  /*@Nullable*/ public Throwable throwable () {
    return throwable;
  }

  public Tuple2<T,Throwable> tuple () {
    return new Tuple2<>(value.orElse(null), throwable);
  }

  public Tuple2<Optional<T>,Throwable> optTuple () {
    return new Tuple2<>(value, throwable);
  }

  // Callable

  @Override @SuppressWarnings("RedundantThrows")
  /*@Nullable*/ public T call () throws Exception {
    if (throwable != null) {
      SeqUtils.sneakyThrow(throwable);// throws Exception or Error!
    }
    return value.orElse(null);
  }//Callable.call

  @SuppressWarnings("RedundantThrows")
  public Optional<T> optCall () throws Exception {
    if (throwable != null) {
      SeqUtils.sneakyThrow(throwable);// throws Exception or Error!
    }
    return value;
  }

  // Supplier

  @Override
  public T get () throws IllegalStateException, NoSuchElementException {
    if (throwable != null) {
      throw new IllegalStateException("Throwable instead of value", throwable);

    } else if (value.isEmpty()) {
      throw new NoSuchElementException("No value present");
    }
    return value.get();
  }//Supplier.get

  // creation

  private Either (Optional<T> nonNullOptionalAsValue, Throwable nullableThrowable) {
    value = nonNullOptionalAsValue;
    throwable = nullableThrowable;
  }//new


  public static <T> Either<T> success (T value) {
    return new Either<>(Optional.ofNullable(value), null);
  }

  public static <T> Either<T> success (Optional<T> optValue) {
    return new Either<>(optValue, null);
  }

  public static <T> Either<T> failure (Throwable t) {
    return new Either<>(Optional.empty(), Objects.requireNonNull(t));
  }

  //

  public boolean isSuccess () {
    return throwable == null;
  }

  public boolean isFailure () {
    return throwable != null;
  }

  /**
   * If a value is present (success and not empty), returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is present, otherwise {@code false}
   */
  public boolean isPresent() {
    return isSuccess() && value.isPresent();
  }

  /**
   * If a value is not present (failure or null), returns {@code true}, otherwise {@code false}.
   *
   * @return  {@code true} if a value is not present, otherwise {@code false}
   */
  public boolean isEmpty() {
    return isFailure() || value.isEmpty();
  }

  /** Success, but null */
  public boolean isNull () {
    return isSuccess() && value.isEmpty();
  }


  public Either<T> throwIfFailure () throws IllegalStateException {
    if (isFailure()) {
      throw new IllegalStateException("Throwable instead of value", throwable);
    }
    return this;
  }

  public Either<T> throwIfEmpty () throws IllegalStateException, NoSuchElementException {
    if (isFailure()) {
      throw new IllegalStateException("Throwable instead of value", throwable);
    } else if (value.isEmpty()) {
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

  public Either<T> optIfPresent (Consumer<Optional<? super T>> action) {
    if (isPresent()) {
      action.accept(value);
    }
    return this;
  }

  public Optional<T> optIfPresent (Optional<T> elseValue) {
    if (isPresent()) {
      return value;
    }
    return elseValue;
  }

  public Either<T> ifPresent (Consumer<? super T> action) {
    if (isPresent()) {
      //noinspection OptionalGetWithoutIsPresent
      action.accept(value.get());
    }
    return this;
  }

  public T ifPresent (T elseValue) {
    if (isPresent()) {
      //noinspection OptionalGetWithoutIsPresent
      value.get();
    }
    return elseValue;
  }


  public Either<T> ifNull (Runnable nullAction) {
    if (isNull()) {
      nullAction.run();
    }
    return this;
  }

  public Either<T> ifEmpty (Runnable nullAction) {
    if (isEmpty()) {
      nullAction.run();
    }
    return this;
  }

  public Either<T> ifEmpty (Consumer<Throwable> failureOrNullAction) {
    if (isEmpty()) {
      failureOrNullAction.accept(throwable);
    }
    return this;
  }

  public Either<T> ifSuccess (Consumer<? super T> nullSafeAction) {
    if (isSuccess()) {
      nullSafeAction.accept(value.orElse(null));
    }
    return this;
  }

  public T ifSuccess (T elseValue) {
    if (isSuccess()) {
      return value.orElse(null);
    }
    return elseValue;
  }

  public Either<T> optIfSuccess (Consumer<Optional<? super T>> action) {
    if (isSuccess()) {
      action.accept(value);
    }
    return this;
  }

  public Optional<T> optIfSuccess (Optional<T> elseValue) {
    if (isSuccess()) {
      return value;
    }
    return elseValue;
  }

  public Either<T> ifFailure (Consumer<Throwable> failureAction) {
    if (isFailure()) {
      failureAction.accept(throwable);
    }
    return this;
  }

  public Optional<Throwable> ifFailure () {
    return Optional.ofNullable(throwable);
  }

  public void consume (BiConsumer<? super T,Throwable> consumer) {
    consumer.accept(value.orElse(null), throwable);
  }

  public void optConsume (BiConsumer<Optional<? super T>,Throwable> consumer) {
    consumer.accept(value, throwable);
  }

  public <R> R map (BiFunction<? super T,Throwable,? extends R> fun) {
    return fun.apply(value.orElse(null), throwable);
  }

  public <R> R optMap (BiFunction<Optional<? super T>,Throwable,? extends R> fun) {
    return fun.apply(value, throwable);
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
      //noinspection OptionalGetWithoutIsPresent
      return Stream.of(value.get());
    }
  }

  public Stream<Optional<T>> optStream () {
    if (isFailure()) {
      return Stream.empty();
    } else {
      return Stream.of(value);
    }
  }


  /**
   * Indicates whether some other object is "equal to" this {@code Optional}.
   * The other object is considered equal if:
   * <ul>
   * <li>it is also an {@code Optional} and;
   * <li>both instances have no value present or;
   * <li>the present values are "equal to" each other via {@code equals()}.
   * </ul>
   *
   * @param obj an object to be tested for equality
   * @return {@code true} if the other object is "equal to" this object
   *         otherwise {@code false}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof Either<?>) {
      Either<?> o = (Either<?>) obj;
      return Objects.equals(value, o.value) && Objects.equals(throwable, o.throwable);

    } else if (obj instanceof Optional<?>) {
      Optional<?> o = (Optional<?>) obj;
      return Objects.equals(value, o.orElse(null));

    } else if (obj instanceof Throwable) {
      Throwable o = (Throwable) obj;
      return Objects.equals(throwable, o);
    }

    return false;
  }

  /**
   * Returns the hash code of the value, if present, otherwise {@code 0}
   * (zero) if no value is present.
   *
   * @return hash code value of the present value or {@code 0} if no value is
   *         present
   */
  @Override public int hashCode() {
    return value.hashCode() ^ Objects.hashCode(throwable);
  }

  /**
   * Returns a non-empty string representation of this {@code Either}
   * suitable for debugging.
   *
   * @return the string representation of this instance
   */
  @Override public String toString () {
    if (isFailure()) {
      return "Either.Failure("+ throwable+')';

    } else if (value.isPresent()) {
      return "Either("+ value.get() +')';
    }
    return "Either.Empty";
  }

  //java.util.concurrent.Future

  public Future<T> future () {
    return new RunnableFuture<>() {
      @Override public boolean isCancelled () {
        return isFailure();
      }

      @Override public boolean isDone () {
        return isSuccess();
      }

      @Override
      /*@Nullable*/ public T get () throws InterruptedException, ExecutionException {
        if (throwable instanceof InterruptedException) {
          throw (InterruptedException) throwable;
        } else if (throwable != null) {
          throw new ExecutionException(throwable);
        }
        return value.orElse(null);
      }

      @Override
      /*@Nullable*/ public T get (long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        return get();
      }

      @Override public boolean cancel (boolean mayInterruptIfRunning) {
        return false; // NOOP
      }

      @Override public void run () {}

      @Override public String toString () {
        return Either.this + ".Future";
      }
    };
  }

}