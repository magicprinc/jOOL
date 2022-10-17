package org.jooq.lambda.fi.util.concurrent;

import org.jooq.lambda.Wrap;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * A "Safe" both-{@link Callable}/{@link CheckedCallable} that allows to throw checked exceptions.<br>
 * Override {@link #execute}, not {@link #call} ()}!<br>
 * You can also override default Throwable handling (and return value other than null) in {@link #handleThrowable}.
 *
 * @author Andrej Fink
 */
public interface SafeCallable<V> extends Callable<V>, CheckedCallable<V>, Supplier<V> {

  /** Override this! */
  V execute () throws Throwable;


  /** Default implementation calls {@link Wrap#handleThrowable},
   which re-throws-if Error, re-sets Thread.interrupted if needed and calls LOGGER.warn */
  default V handleThrowable (Throwable t) throws Error {
    Wrap.handleThrowable(this, t);
    return null;
  }


  @Override default V get () throws Error {
    return call();
  }

  @Override default V call () throws Error {
    try {
      return execute();
    } catch (Throwable t) {
      return handleThrowable(t);
    }
  }

  /** Used in {@link Wrap#callable}, {@link Wrap#callable(CheckedCallable, java.util.function.Function)}
   and {@link Wrap#callableDef} */
  interface Generated<V> extends SafeCallable<V> {
    @Override default V get () {
      return execute();
    }
    @Override default V call () {
      return execute();
    }
    @Override V execute ();
  }
}