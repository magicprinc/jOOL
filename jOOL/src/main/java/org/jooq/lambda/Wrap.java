package org.jooq.lambda;

import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.jooq.lambda.fi.util.concurrent.CheckedCallable;
import org.jooq.lambda.fi.util.concurrent.SafeCallable;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.jooq.lambda.fi.util.function.CheckedBiPredicate;
import org.jooq.lambda.fi.util.function.CheckedDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedDoublePredicate;
import org.jooq.lambda.fi.util.function.CheckedDoubleToIntFunction;
import org.jooq.lambda.fi.util.function.CheckedDoubleToLongFunction;
import org.jooq.lambda.fi.util.function.CheckedFunction;
import org.jooq.lambda.fi.util.function.CheckedIntFunction;
import org.jooq.lambda.fi.util.function.CheckedIntPredicate;
import org.jooq.lambda.fi.util.function.CheckedIntToDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedIntToLongFunction;
import org.jooq.lambda.fi.util.function.CheckedLongFunction;
import org.jooq.lambda.fi.util.function.CheckedLongPredicate;
import org.jooq.lambda.fi.util.function.CheckedLongToDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedLongToIntFunction;
import org.jooq.lambda.fi.util.function.CheckedPredicate;
import org.jooq.lambda.fi.util.function.CheckedToDoubleBiFunction;
import org.jooq.lambda.fi.util.function.CheckedToDoubleFunction;
import org.jooq.lambda.fi.util.function.CheckedToIntBiFunction;
import org.jooq.lambda.fi.util.function.CheckedToIntFunction;
import org.jooq.lambda.fi.util.function.CheckedToLongBiFunction;
import org.jooq.lambda.fi.util.function.CheckedToLongFunction;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Predicate2;
import org.jooq.lambda.function.Predicate3;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple0;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple10;
import org.jooq.lambda.tuple.Tuple11;
import org.jooq.lambda.tuple.Tuple12;
import org.jooq.lambda.tuple.Tuple13;
import org.jooq.lambda.tuple.Tuple14;
import org.jooq.lambda.tuple.Tuple15;
import org.jooq.lambda.tuple.Tuple16;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;
import org.jooq.lambda.tuple.Tuple9;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

/**
 Improved interoperability between checked exceptions and Java 8.<br>
 Sibling of {@link Unchecked} and {@link Sneaky}.<br>
 Similar to them, but instead of throwing Exception (Checked or Unchecked) "wraps" it in return value.
 Designed especially for Stream APIs, where final selection is important, not a single function call.
 <br>
 Wrap ~ Safe.

 @author Andrej Fink
 */
public class Wrap {
  private static final LambdaLogFacade LOGGER = LambdaLogFacade.getLogger(Wrap.class);
  private Wrap () {}

  // Additional Unchecked Consumer<Throwable>

  /**
   * A {@link Consumer} that silently ignores all checked {@link Exception}s,
   * but re-throws {@link RuntimeException} and {@link Error}.
   */
  public static final Consumer<Throwable> SILENT_IGNORE = Wrap::throwIfErrorOrRuntime;

  /**
   * A {@link Consumer} that silently ignores all checked {@link Exception} AND
   * {@link RuntimeException}s, but re-throws  {@link Error}.
   * (It is a very bad idea, to silently ignore Errors)
   * <br>
   * Use it with LOG_WARN or PRINT_STACK_TRACE to prevent problems being unobserved:
   * SILENT_IGNORE_ALL.andThen(LOG_WARN)
   */
  public static final Consumer<Throwable> SILENT_IGNORE_ALL = Wrap::throwIfError;


  /**
   * Must-be-the-LAST {@link Consumer} that calls log.warn(msg, Throwable).
   * Usage ~ SILENT_IGNORE_ALL.andThen(LOG_WARN)
   */
  public static final Consumer<Throwable> LOG_WARN = t -> LOGGER.warn("C_LOG_WARN", t);

  /**
   * Must-be-the-LAST {@link Consumer} that calls Throwable::printStackTrace.
   * Usage ~ SILENT_IGNORE_ALL.andThen(PRINT_STACK_TRACE)
   */
  public static final Consumer<Throwable> PRINT_STACK_TRACE = Throwable::printStackTrace;

  /**
   Can be used in tests:
   Thread.currentThread().setUncaughtExceptionHandler(Wrap.LOGGING_UNCAUGHT_EXCEPTION_HANDLER);
   Thread.setDefaultUncaughtExceptionHandler(Wrap.LOGGING_UNCAUGHT_EXCEPTION_HANDLER);
   */
  public static final Thread.UncaughtExceptionHandler LOG_WARN_UNCAUGHT_EXCEPTION_HANDLER = LOGGER;

  // Safe

  public static final Predicate2<Object,Throwable> P1_SILENT_IGNORE_ALL_FALSE = (a,t) -> silentIgnoreAll(t, false);

  public static final Predicate2<Object,Throwable> P1_SILENT_IGNORE_ALL_TRUE = (a,t) -> silentIgnoreAll(t, true);

  public static final Predicate2<Object,Throwable> P1_LOG_WARN_FALSE = (a,t) -> logWarn(t, false, a);

  public static final Predicate2<Object,Throwable> P1_LOG_WARN_TRUE = (a,t) -> logWarn(t, true, a);


  public static final Predicate3<Object,Object,Throwable> P2_SILENT_IGNORE_ALL_FALSE = (a,b,t) -> silentIgnoreAll(t, false);

  public static final Predicate3<Object,Object,Throwable> P2_SILENT_IGNORE_ALL_TRUE = (a,b,t) -> silentIgnoreAll(t, true);

  public static final Predicate3<Object,Object,Throwable> P2_LOG_WARN_FALSE = (a,b,t) -> logWarn(t, false, a, b);

  public static final Predicate3<Object,Object,Throwable> P2_LOG_WARN_TRUE = (a,b,t) -> logWarn(t, true, a, b);


  private static boolean silentIgnoreAll (Throwable t, boolean returnValue) {
    throwIfError(t);
    return returnValue;
  }

  private static boolean logWarn (Throwable t, boolean returnValue, Object a) {
    LOGGER.warn("P1_LOG_WARN_"+returnValue+": "+a, t);
    return returnValue;
  }

  private static boolean logWarn (Throwable t, boolean returnValue, Object a, Object b) {
    LOGGER.warn("P2_LOG_WARN_"+returnValue+": "+a+", "+b, t);
    return returnValue;
  }


  private static <T> T logWarnFun (Throwable t, T returnValue, Object a) {
    LOGGER.warn("F1_LOG_WARN_"+returnValue+": "+a, t);
    return returnValue;
  }

  private static <T> T logWarnFun (Throwable t, T returnValue, Object a, Object b) {
    LOGGER.warn("F2_LOG_WARN_"+returnValue+": "+a+", "+b, t);
    return returnValue;
  }


  /**
   * This method can be used by code that is deliberately violating the
   * allowed checked casts. Rather than marking the whole method containing
   * the code with @SuppressWarnings, you can use a call to this method for
   * the exact place where you need to escape the constraints. Typically,
   * you will "import static" this method and then write either<br>
   * X x = cast(y);<br>
   * or, if that doesn't work (e.g. X is a type variable)<br>
   *    Cast.&lt;X&gt;cast(y);
   */
  @SuppressWarnings({"unchecked","TypeParameterUnusedInFormals"})
  public static <T> T castUnsafe (Object unsafe) throws ClassCastException, NullPointerException {
    return (T) unsafe;
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast (Class<? super T> toClazz, Object safer) throws ClassCastException, NullPointerException {
    return (T) toClazz.cast(safer);
  }


  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T extends Tuple> T tuple (Object... v) {
    if (v == null || v.length == 0) {
      return (T) new Tuple0();
    }
    switch (v.length) {
      case 1: return (T) new Tuple1(v[0]);
      case 2: return (T) new Tuple2(v[0],v[1]);
      case 3: return (T) new Tuple3(v[0],v[1],v[2]);
      case 4: return (T) new Tuple4(v[0],v[1],v[2],v[3]);
      case 5: return (T) new Tuple5(v[0],v[1],v[2],v[3],v[4]);
      case 6: return (T) new Tuple6(v[0],v[1],v[2],v[3],v[4],v[5]);
      case 7: return (T) new Tuple7(v[0],v[1],v[2],v[3],v[4],v[5],v[6]);
      case 8: return (T) new Tuple8(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7]);
      case 9: return (T) new Tuple9(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8]);
      case 10: return(T)new Tuple10(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9]);
      case 11: return(T)new Tuple11(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10]);
      case 12: return(T)new Tuple12(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11]);
      case 13: return(T)new Tuple13(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12]);
      case 14: return(T)new Tuple14(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12],v[13]);
      case 15: return(T)new Tuple15(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12],v[13],v[14]);
      case 16: return(T)new Tuple16(v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7],v[8],v[9],v[10],v[11],v[12],v[13],v[14],v[15]);
      default: throw new IllegalArgumentException("Unknown Tuple degree: "+v.length+" "+ Arrays.toString(v));
    }
  }


  /** Same as {@link #handleInterruptedException(Throwable)}, but without casting.
   Will be automatically used in "catch (InterruptedException e)" */
  public static void handleInterruptedException (InterruptedException ie) {
    Thread.currentThread().interrupt();
  }

  /** if t is Throwable → re-set Thread.interrupted.
   [#230] Clients will not expect needing to handle this. */
  public static void handleInterruptedException (Throwable t) {
    if (t instanceof InterruptedException)
      Thread.currentThread().interrupt();
  }

  public static void throwIfError (Throwable t) throws Error {
    if (t instanceof Error)
      throw (Error) t;

    handleInterruptedException(t);
  }

  public static void throwIfErrorOrRuntime (Throwable t) throws RuntimeException, Error {
    throwIfError(t);

    if (t instanceof RuntimeException)
      throw (RuntimeException) t;
  }

  public static void throwIfErrorOrRuntimeOrIo (Throwable t) throws UncheckedIOException, RuntimeException, Error {
    throwIfErrorOrRuntime(t);

    if (t instanceof IOException)
      throw new UncheckedIOException((IOException) t);
  }

  /** [Unchecked.handleThrowable]
   An Unchecked strategy to handle Throwable: wrap any {@link Throwable} in a {@link RuntimeException}
   */
  public static void throwUnchecked(Throwable t) throws UncheckedException, UncheckedIOException, RuntimeException, Error {
    throwIfErrorOrRuntimeOrIo(t);

    throw new UncheckedException(t);
  }


  /** A "Silent+log.warn" strategy to handle Throwable: re-throws Error, re-sets Thread.interrupted,
   calls log.warn and ignores everything else.
   @see #throwIfError */
  public static void handleThrowable (Object action, Throwable t) throws Error {
    throwIfError(t);
    LOGGER.warn("handleThrowable in "+action, t);
  }


  // Direct calls


  public static boolean run (CheckedRunnable runSafely) /*throws Error*/ {
    try {
      runSafely.run();
      return true;//success
    } catch (Throwable t) {
      handleThrowable(runSafely, t);
    }
    return false;//failure
  }

  public static boolean runStd (Runnable runSafely) /*throws Error*/ {
    try {
      runSafely.run();
      return true;//success
    } catch (Throwable t) {
      handleThrowable(runSafely, t);
    }
    return false;//failure
  }


  public static <T> Either<T> call (CheckedCallable<T> callSafely) {
    try {
      return Either.success(callSafely.call());
    } catch (Throwable t) {
      return Either.failure(t);
    }
  }

  public static <T> Either<T> callStd (Callable<T> callSafely) {
    try {
      return Either.success(callSafely.call());
    } catch (Throwable t) {
      return Either.failure(t);
    }
  }


  // Runnable


  /** Wrap as safe {@link Runnable}: which logs Exceptions, doesn't throw them.
   @see org.jooq.lambda.fi.lang.SafeRunnable */
  public static Runnable runnable (CheckedRunnable runnable) {
    return () -> {
      try {
        runnable.run();
      } catch (Throwable t) {
        handleThrowable(runnable, t);
      }
    };
  }


  // Callable


  /** Wraps as safe {@link Callable}: which returns {@link Either}, doesn't throw Exceptions.
   @see SafeCallable */
  public static <V> SafeCallable<Either<V>> callable (CheckedCallable<V> callable) {
    return (SafeCallable.Generated<Either<V>>)() -> {
      try {
        return Either.success(callable.call());
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }

  public static <V> SafeCallable<V> callableDef (CheckedCallable<V> callable, V ifFailure) {
    return (SafeCallable.Generated<V>)() -> {
      try {
        return callable.call();
      } catch (Throwable t) {
        handleThrowable(callable, t);
        return ifFailure;
      }
    };
  }

  public static <V> SafeCallable<V> callable (CheckedCallable<V> callable, Function<Throwable,V> handler) {
    return (SafeCallable.Generated<V>)() -> {
      try {
        return callable.call();
      } catch (Throwable t) {
        return handler.apply(t);
      }
    };
  }

  // Predicates

  public static <T> Predicate<T> predicate (CheckedPredicate<T> unsafePredicate) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, false, value);
      }
    };
  }

  public static <T> Predicate<T> predicate (CheckedPredicate<T> unsafePredicate, BiPredicate<T,Throwable> fallbackHandler) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        return fallbackHandler.test(value, t);
      }
    };
  }

  public static <T> Predicate<T> predicateDef (CheckedPredicate<T> unsafePredicate, boolean def) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, def, value);
      }
    };
  }


  public static <T,U> BiPredicate<T,U> biPredicate (CheckedBiPredicate<T,U> unsafePredicate) {
    return (a,b) -> {
      try {
        return unsafePredicate.test(a,b);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, false, a, b);
      }
    };
  }

  public static <T,U> BiPredicate<T,U> biPredicate (CheckedBiPredicate<T,U> unsafePredicate, Predicate3<T,U,Throwable> fallbackHandler) {
    return (a,b) -> {
      try {
        return unsafePredicate.test(a,b);
      } catch (Throwable t) {
        return fallbackHandler.test(a,b,t);
      }
    };
  }

  public static <T,U> BiPredicate<T,U> biPredicateDef (CheckedBiPredicate<T,U> unsafePredicate, boolean def) {
    return (a,b) -> {
      try {
        return unsafePredicate.test(a,b);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, def, a, b);
      }
    };
  }


  public static DoublePredicate doublePredicate (CheckedDoublePredicate unsafePredicate) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, false, value);
      }
    };
  }

  public static DoublePredicate doublePredicate (CheckedDoublePredicate unsafePredicate, BiPredicate<Double,Throwable> fallbackHandler) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        return fallbackHandler.test(value, t);
      }
    };
  }

  public static DoublePredicate doublePredicateDef (CheckedDoublePredicate unsafePredicate, boolean def) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, def, value);
      }
    };
  }


  public static LongPredicate longPredicate (CheckedLongPredicate unsafePredicate) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, false, value);
      }
    };
  }

  public static LongPredicate longPredicate (CheckedLongPredicate unsafePredicate, BiPredicate<Long,Throwable> fallbackHandler) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        return fallbackHandler.test(value, t);
      }
    };
  }

  public static LongPredicate longPredicateDef (CheckedLongPredicate unsafePredicate, boolean def) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, def, value);
      }
    };
  }


  public static IntPredicate intPredicate (CheckedIntPredicate unsafePredicate) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, false, value);
      }
    };
  }

  public static IntPredicate intPredicate (CheckedIntPredicate unsafePredicate, BiPredicate<Integer,Throwable> fallbackHandler) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        return fallbackHandler.test(value, t);
      }
    };
  }

  public static IntPredicate intPredicateDef (CheckedIntPredicate unsafePredicate, boolean def) {
    return (value) -> {
      try {
        return unsafePredicate.test(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarn(t, def, value);
      }
    };
  }


  // Functions


  public static <T,R> Function<T,Either<R>> function (CheckedFunction<T,R> unsafeFunction) {
    return (value) -> {
      try {
        return Either.success(unsafeFunction.apply(value));
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }

  public static <T,R> Function<T,R> function (CheckedFunction<T,R> unsafeFunction, BiFunction<T,Throwable,R> handler) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static <T,R> Function<T,R> functionDef (CheckedFunction<T,R> unsafeFunction, R def) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static <T,U,R> BiFunction<T,U,Either<R>> biFunction (CheckedBiFunction<T,U,R> unsafeFunction) {
    return (a,b) -> {
      try {
        return Either.success(unsafeFunction.apply(a,b));
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }

  public static <T,U,R> BiFunction<T,U,R> biFunction (CheckedBiFunction<T,U,R> unsafeFunction, Function3<T,U,Throwable,R> handler) {
    return (a,b) -> {
      try {
        return unsafeFunction.apply(a,b);
      } catch (Throwable t) {
        return handler.apply(a,b, t);
      }
    };
  }

  public static <T,U,R> BiFunction<T,U,R> biFunctionDef (CheckedBiFunction<T,U,R> unsafeFunction, R def) {
    return (a,b) -> {
      try {
        return unsafeFunction.apply(a,b);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, a,b);
      }
    };
  }


  public static <R> DoubleFunction<Either<R>> doubleFunction (CheckedDoubleFunction<R> unsafeFunction) {
    return (value) -> {
      try {
        return Either.success(unsafeFunction.apply(value));
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }

  public static <R> DoubleFunction<R> doubleFunction (CheckedDoubleFunction<R> unsafeFunction, BiFunction<Double,Throwable,R> handler) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static <R> DoubleFunction<R> doubleFunctionDef (CheckedDoubleFunction<R> unsafeFunction, R def) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static DoubleToIntFunction doubleToIntFunction (CheckedDoubleToIntFunction unsafeFunction, BiFunction<Double,Throwable,Integer> handler) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsInt(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static DoubleToIntFunction doubleToIntFunctionDef (CheckedDoubleToIntFunction unsafeFunction, int def) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsInt(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static DoubleToLongFunction doubleToLongFunction (CheckedDoubleToLongFunction unsafeFunction, BiFunction<Double,Throwable,Long> handler) {
    return (double value) -> {
      try {
        return unsafeFunction.applyAsLong(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static DoubleToLongFunction doubleToLongFunctionDef (CheckedDoubleToLongFunction unsafeFunction, long def) {
    return (double value) -> {
      try {
        return unsafeFunction.applyAsLong(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static <R> IntFunction<Either<R>> intFunction (CheckedIntFunction<R> unsafeFunction) {
    return (value) -> {
      try {
        return Either.success(unsafeFunction.apply(value));
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }

  public static <R> IntFunction<R> intFunction (CheckedIntFunction<R> unsafeFunction, BiFunction<Integer,Throwable,R> handler) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static <R> IntFunction<R> intFunctionDef (CheckedIntFunction<R> unsafeFunction, R def) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static IntToDoubleFunction intToDoubleFunction (CheckedIntToDoubleFunction unsafeFunction, BiFunction<Integer,Throwable,Double> handler) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsDouble(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static IntToDoubleFunction intToDoubleFunctionDef (CheckedIntToDoubleFunction unsafeFunction, double def) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsDouble(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static IntToLongFunction intToLongFunction (CheckedIntToLongFunction unsafeFunction, BiFunction<Integer,Throwable,Long> handler) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsLong(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static IntToLongFunction intToLongFunctionDef (CheckedIntToLongFunction unsafeFunction, long def) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsLong(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static <R> LongFunction<Either<R>> longFunction (CheckedLongFunction<R> unsafeFunction) {
    return (value) -> {
      try {
        return Either.success(unsafeFunction.apply(value));
      } catch (Throwable t) {
        return Either.failure(t);
      }
    };
  }

  public static <R> LongFunction<R> longFunction (CheckedLongFunction<R> unsafeFunction, BiFunction<Long,Throwable,R> handler) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static <R> LongFunction<R> longFunctionDef (CheckedLongFunction<R> unsafeFunction, R def) {
    return (value) -> {
      try {
        return unsafeFunction.apply(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static LongToDoubleFunction longToDoubleFunction (CheckedLongToDoubleFunction unsafeFunction, BiFunction<Long,Throwable,Double> handler) {
    return (longValue) -> {
      try {
        return unsafeFunction.applyAsDouble(longValue);
      } catch (Throwable t) {
        return handler.apply(longValue, t);
      }
    };
  }

  public static LongToDoubleFunction longToDoubleFunctionDef (CheckedLongToDoubleFunction unsafeFunction, double def) {
    return (longValue) -> {
      try {
        return unsafeFunction.applyAsDouble(longValue);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, longValue);
      }
    };
  }


  public static LongToIntFunction longToIntFunction (CheckedLongToIntFunction unsafeFunction, BiFunction<Long,Throwable,Integer> handler) {
    return (longValue) -> {
      try {
        return unsafeFunction.applyAsInt(longValue);
      } catch (Throwable t) {
        return handler.apply(longValue, t);
      }
    };
  }

  public static LongToIntFunction longToIntFunctionDef (CheckedLongToIntFunction unsafeFunction, int def) {
    return (longValue) -> {
      try {
        return unsafeFunction.applyAsInt(longValue);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, longValue);
      }
    };
  }


  public static <T,U> ToDoubleBiFunction<T,U> toDoubleBiFunction (CheckedToDoubleBiFunction<T,U> unsafeFunction, Function3<T,U,Throwable,Double> handler) {
    return (a,b) -> {
      try {
        return unsafeFunction.applyAsDouble(a,b);
      } catch (Throwable t) {
        return handler.apply(a,b, t);
      }
    };
  }

  public static <T,U> ToDoubleBiFunction<T,U> toDoubleBiFunctionDef (CheckedToDoubleBiFunction<T,U> unsafeFunction, double def) {
    return (a,b) -> {
      try {
        return unsafeFunction.applyAsDouble(a,b);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, a,b);
      }
    };
  }


  public static <T> ToDoubleFunction<T> toDoubleFunction (CheckedToDoubleFunction<T> unsafeFunction, BiFunction<T,Throwable,Double> handler) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsDouble(value);
      } catch (Throwable t) {
        return handler.apply(value, t);
      }
    };
  }

  public static <T> ToDoubleFunction<T> toDoubleFunctionDef (CheckedToDoubleFunction<T> unsafeFunction, double def) {
    return (value) -> {
      try {
        return unsafeFunction.applyAsDouble(value);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, value);
      }
    };
  }


  public static <T,U> ToIntBiFunction<T,U> toIntBiFunction (CheckedToIntBiFunction<T,U> unsafeFunction, Function3<T,U,Throwable,Integer> handler) {
    return (a,b) -> {
      try {
        return unsafeFunction.applyAsInt(a,b);
      } catch (Throwable t) {
        return handler.apply(a,b, t);
      }
    };
  }

  public static <T,U> ToIntBiFunction<T,U> toIntBiFunctionDef (CheckedToIntBiFunction<T,U> unsafeFunction, int def) {
    return (a,b) -> {
      try {
        return unsafeFunction.applyAsInt(a,b);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, a,b);
      }
    };
  }


  public static <T> ToIntFunction<T> toIntFunction (CheckedToIntFunction<T> unsafeFunction, BiFunction<T,Throwable,Integer> handler) {
    return (T v) -> {
      try {
        return unsafeFunction.applyAsInt(v);
      } catch (Throwable t) {
        return handler.apply(v, t);
      }
    };
  }

  public static <T> ToIntFunction<T> toIntFunctionDef (CheckedToIntFunction<T> unsafeFunction, int def) {
    return (T v) -> {
      try {
        return unsafeFunction.applyAsInt(v);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, v);
      }
    };
  }


  public static <T,U> ToLongBiFunction<T,U> toLongBiFunction (CheckedToLongBiFunction<T,U> unsafeFunction, Function3<T,U,Throwable,Long> handler) {
    return (a,b) -> {
      try {
        return unsafeFunction.applyAsLong(a,b);
      } catch (Throwable t) {
        return handler.apply(a,b, t);
      }
    };
  }

  public static <T,U> ToLongBiFunction<T,U> toLongBiFunctionDef (CheckedToLongBiFunction<T,U> unsafeFunction, long def) {
    return (a,b) -> {
      try {
        return unsafeFunction.applyAsLong(a,b);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, a,b);
      }
    };
  }


  public static <T> ToLongFunction<T> toLongFunction (CheckedToLongFunction<T> unsafeFunction, BiFunction<T,Throwable,Long> handler) {
    return (T v) -> {
      try {
        return unsafeFunction.applyAsLong(v);
      } catch (Throwable t) {
        return handler.apply(v, t);
      }
    };
  }

  public static <T> ToLongFunction<T> toLongFunctionDef (CheckedToLongFunction<T> unsafeFunction, long def) {
    return (T v) -> {
      try {
        return unsafeFunction.applyAsLong(v);
      } catch (Throwable t) {
        throwIfError(t);
        return logWarnFun(t, def, v);
      }
    };
  }

}