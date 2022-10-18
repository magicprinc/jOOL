package org.jooq.lambda;

import org.jooq.lambda.tuple.Tuple4;

import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

/** External lambda "for"-loops. */
public class Loops {
  private Loops () {}//static utils


  /**
   Simple loop (numberOfRepetitions .. 0].
   <pre>{@code
   while (numberOfRepetitions-- > 0)
     body.run();
   }</pre>
   */
  public static void loop (long numberOfRepetitions, Runnable body) {
    while (numberOfRepetitions-- > 0) {
      body.run();
    }
  }

  /**
   Simple while-loop (numberOfRepetitions .. 0].
   <pre>{@code}
   while (numberOfRepetitions-- &gt; 0)
     body.accept(numberOfRepetitions);
   }</pre>
   */
  public static void loop (long numberOfRepetitions, LongConsumer body) {
    while (numberOfRepetitions-- > 0) {
      body.accept(numberOfRepetitions);
    }
  }

  /**
   Simple for-loop [0 .. numberOfRepetitions).
   <pre>{@code}
   for (int i=0; i&lt;max; i++)
     body.accept(i);
   }</pre>
   */
  public static void forLoop (int maxExclusive, IntConsumer body) {
    for (int i=0; i<maxExclusive; i++) {
      body.accept(i);
    }
  }

  /**
   Simple loop (numberOfRepetitions .. 0] with extra condition.
   <pre>{@code
   while (numberOfRepetitions-- > 0 && body.test(numberOfRepetitions)) {}
   }</pre>
   @param body do-while Predicate: loops while it is true and numberOfRepetitions &gt; 0
   */
  public static void loopWhile (long numberOfRepetitions, LongPredicate body) {
    //noinspection StatementWithEmptyBody
    while (numberOfRepetitions-- > 0 && body.test(numberOfRepetitions)) {
      // do while
    }
  }

  // Ad-Hoc JMH (not for serious benchmarks)

  static final long NANO = 1000_000_000L;

  /** Similar to {@link #loop(long, Runnable)}, but with metrics */
  public static Tuple4<Long, LongSummaryStatistics, Exception, String> loopMeasured (long numberOfRepetitions, Runnable body) {
    LongSummaryStatistics st = new LongSummaryStatistics();
    Exception ex = null;
    long n = System.nanoTime();

    while (numberOfRepetitions-- > 0) {
      long t = System.nanoTime();
      try {
        body.run();
      } catch (Exception showStopper) {
        ex = showStopper;
        break;
      } finally {
        st.accept(System.nanoTime() - t);
      }
    }
    n = System.nanoTime() - n;

    StringBuilder sb = new StringBuilder(200);// ≤ ≥
    sb.append("LOOP: ").append(st.getCount())
        .append(" nanos= ").append(st.getSum()).append(" ≤ ").append(n)
        .append(" ~ op/sec= ");
    if (st.getSum() > 0) {
      sb.append((st.getCount() * NANO) / st.getSum()).append(" ≥ ");
    }
    sb.append((st.getCount() * NANO) / Math.max(n, 1));
    if (st.getCount()>0) {
      sb.append(" ~ nano/op= ").append(st.getSum() / st.getCount()).append(" ≤ ").append(n / st.getCount());
    }
    sb.append(" min<avg<max= ").append(st.getMin())
        .append(" < ").append(st.getAverage())
        .append(" < ").append(st.getMax());
    return new Tuple4<>(n, st, ex, sb.toString());
  }

  /** Similar to {@link #loop(long, Runnable)}, but with metrics and warm-up */
  public static Tuple4<Long, LongSummaryStatistics, Exception, String> loopMeasuredWarm (long numberOfRepetitions, Runnable body) {
    long warmCnt = Math.min(50_000, numberOfRepetitions/10+10);

    loop(warmCnt, body);

    return loopMeasured(numberOfRepetitions, body);
  }


  public static class Incrementer {
    protected final int[] indexes;
    private final IntUnaryOperator maxForIndex;

    /** All indexes have the same max and rotate in range [0..maxExclusive)
     @param degree size of index vector = how many nested for-loops
     @param maxExclusive number of repetitions: every index variable rotates in range [0..maxExclusive)
     */
    public Incrementer (int degree, final int maxExclusive) {
      assert degree > 0 : "degree must be > 0, but "+degree;
      assert maxExclusive > 0 : "maxExclusive must be > 0, but "+maxExclusive;

      indexes = new int[degree];
      maxForIndex = (index) -> maxExclusive;
      init();
    }//new

    /**
     * Permutation with repetition. Every index has its own max.
     * @param maxExclusive all indexes have their own max and rotate in range [0..maxExclusive[index])
     */
    public Incrementer (final int[] maxExclusive) {
      assert maxExclusive.length > 0 : "degree must be > 0, but "+maxExclusive.length;

      indexes = new int[maxExclusive.length];
      maxForIndex = (index) -> maxExclusive[index];
      init();
    }//new

    protected void init () {}

    public int degree () {
      return indexes.length;
    }

    public int indexAt (int indexIndex) {
      return indexes[indexIndex];
    }

    public int maxAt (int indexIndex) {
      return maxForIndex.applyAsInt(indexIndex);
    }

    @Override public String toString () {
      return "Incrementer"+ Arrays.toString(indexes);
    }


    /**
     Increments index vector (array with indexes) by 1.
     E.g. for base 2: 0,0,0→0,0,1→0,1,0
     Emulates one step in multi-nested-for loop.

     @return we "overflowed" the array size = all indexes have reached their max-1
     @see #forLoop(Predicate)
     */
    public boolean incrementIndexVector () {
      final int len = indexes.length;

      for (int i = 0; i < len; ) {
        if (++indexes[i] < maxForIndex.applyAsInt(i)) {
          return false;

        } else {// overflow
          indexes[i] = 0;// e.g. [10]: 09 → 10
          i++;
        }
      }
      return true;
    }

    /**
     * Permutation with repetition.
     * @param loopBody receive this Incrementer with indexes and maxExclusive, must return boolean searchIsOver/found
     * @return searchIsOver/found
     */
    public boolean forLoop (Predicate<Incrementer> loopBody) {
      while (true) {
        boolean found = loopBody.test(this);
        if (found) {
          return true;
        }
        boolean overflow = incrementIndexVector();
        if (overflow) {
          return false;
        }
      }
    }
  }//Incrementer

}