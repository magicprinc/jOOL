package org.jooq.lambda.fi.lang;

import org.jooq.lambda.Wrap;

/**
 * A "Safe" both-{@link Runnable}/{@link CheckedRunnable} that allows to throw checked exceptions.<br>
 * Override {@link #execute}, not {@link #run}!<br>
 * You can also override default Throwable handling in {@link #handleThrowable}.
 *
 * @author Andrej Fink
 */
public interface SafeRunnable extends Runnable, CheckedRunnable {

  @Override default void run () throws Error {
    try {
      execute();
    } catch (Throwable t) {
      handleThrowable(t);
    }
  }

  /** Default implementation calls {@link Wrap#handleThrowable},
   which re-throws-if Error, re-sets Thread.interrupted if needed and calls LOGGER.warn */
  default void handleThrowable (Throwable t) throws Error {
    Wrap.handleThrowable(this, t);
  }

  /** Override this! */
  void execute () throws Throwable;

}