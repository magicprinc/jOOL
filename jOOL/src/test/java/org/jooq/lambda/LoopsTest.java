package org.jooq.lambda;

import org.jooq.lambda.tuple.Tuple4;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import static org.jooq.lambda.Loops.loop;
import static org.jooq.lambda.Loops.loopMeasured;
import static org.jooq.lambda.Loops.loopMeasuredWarm;
import static org.jooq.lambda.Loops.loopWhile;
import static org.junit.Assert.*;

public class LoopsTest {

  static class LongSummaryStatisticsP extends LongSummaryStatistics implements LongPredicate {
    int stopAt;

    public LongSummaryStatisticsP (int min) { stopAt = min; }//new
    public LongSummaryStatisticsP () {}//new

    @Override public boolean test (long value) {
      if (value < stopAt) return false;//stop
      accept(value);
      return true;
    }
  }

  @Test public void testLoopRunnable () {
    loop(0, (Runnable) null);
    loop(-2, (Runnable) null);
    AtomicLong counter = new AtomicLong();
    Runnable r = counter::incrementAndGet;

    loop(0, r);
    assertEquals(0, counter.get());

    loop(-1, r);
    assertEquals(0, counter.get());

    loop(1, r);
    assertEquals(1, counter.get());

    loop(10_123, r);
    assertEquals(10_124, counter.get());
  }

  @Test public void testLoopConsumer () {
    loop(0, (LongConsumer) null);
    loop(-2, (LongConsumer) null);
    final LongSummaryStatistics stat = new LongSummaryStatistics();

    loop(0, stat);
    assertEquals(0, stat.getCount());

    loop(-1, stat);
    assertEquals(0, stat.getCount());

    loop(1, stat);
    assertEquals(1, stat.getCount());

    loop(10_123, stat);
    assertEquals(10_124, stat.getCount());
    assertEquals(10_122, stat.getMax());// 10_123 = 10_122..0
    assertEquals(0, stat.getMin());
  }

  @Test public void testLoopPredicate () {
    loopWhile(0, null);
    loopWhile(-2, null);
    final LongSummaryStatisticsP s = new LongSummaryStatisticsP();

    loopWhile(0, s);
    assertEquals(0, s.getCount());

    loopWhile(-1, s);
    assertEquals(0, s.getCount());

    loopWhile(1, s);
    assertEquals(1, s.getCount());

    loopWhile(10_123, s);
    assertEquals(10_124, s.getCount());
    assertEquals(10_122, s.getMax());// 10_123 = 10_122..0
    assertEquals(0, s.getMin());
  }

  @Test public void testLoopPredicateDoWhile () {
    final LongSummaryStatisticsP s = new LongSummaryStatisticsP(10);

    loopWhile(0, s);
    assertEquals(0, s.getCount());

    loopWhile(-1, s);
    assertEquals(0, s.getCount());

    loopWhile(1, s);
    assertEquals(0, s.getCount());

    loopWhile(10_123, s);
    assertEquals(10_123-10, s.getCount());//10 = 0..9
    assertEquals(10_122, s.getMax());// 10_123 is max, starting with 10_122
    assertEquals(10, s.getMin());
  }


  @Test public void testLoopMeasured () {
    Tuple4<Long, LongSummaryStatistics, Exception, String> st = loopMeasured(0, null);
    System.out.println(st);
    assertEquals("LongSummaryStatistics{count=0, sum=0, min=9223372036854775807, average=0,000000, max=-9223372036854775808}", st.v2.toString());
    st = loopMeasured(0, null);
    assertEquals(0, st.v2.getCount());
    st = loopMeasured(-2, null);
    assertEquals(0, st.v2.getCount());

    st = loopMeasured(1, null);
    assertTrue(st.v3.toString().startsWith("java.lang.NullPointerException: Cannot invoke \"java.lang.Runnable.run()\" because \"body\" is null"));
    assertEquals(1, st.v2.getCount());

    final AtomicLong counter = new AtomicLong();
    final Runnable r = counter::incrementAndGet;

    st = loopMeasured(0, r);
    System.out.println(st);
    assertEquals(0, counter.get());
    assertEquals(0, st.v2.getCount());

    st = loopMeasured(-1, r);
    assertEquals(0, counter.get());
    assertEquals(0, st.v2.getCount());

    st = loopMeasured(1, r);
    System.out.println(st);
    assertEquals(1, counter.get());
    assertEquals(1, st.v2.getCount());

    st = loopMeasured(10_123, r);
    System.out.println(st);
    assertEquals(10_124, counter.get());
    assertEquals(10_123, st.v2.getCount());
  }


  @Test public void testLoopMeasuredWarm () {
    final AtomicLong counter = new AtomicLong();
    Tuple4<Long, LongSummaryStatistics, Exception, String> st = loopMeasuredWarm(10_000_000, counter::incrementAndGet);
    System.out.println(st);
    assertEquals(10_000_000, st.v2.getCount());
    assertEquals(10_050_000, counter.get());
  }


  @Test public void testIncrement () {
    Loops.Incrementer x = new Loops.Incrementer(3, 100);
    assertEquals(3, x.degree());
    for (int i=0; i<100; i++) {
      for (int j=0; j<100; j++) {
        for (int k=0; k<100; k++) {
          assertEquals(i, x.indexes[2]);
          assertEquals(j, x.indexes[1]);
          assertEquals(k, x.indexAt(0));
          boolean overflow = x.incrementIndexVector();
          assertEquals(i==99 && j==99 && k==99, overflow);
        }
      }
    }

    x = new Loops.Incrementer(16, 2);
    assertEquals(16, x.degree());
    for (int i=0; i<0x1_00_00; i++) {
      int tmp = i;
      for (int j=0; j<16; j++) {
        assertEquals(tmp & 1, x.indexes[j]);
        tmp = tmp >> 1;
      }
      boolean overflow = x.incrementIndexVector();// 0..1
      assertEquals(i==0xFF_FF, overflow);
    }

    x = new Loops.Incrementer(5000, 1);
    assertEquals(5000, x.degree());
    assertTrue(x.incrementIndexVector());//only one valid state 0,0,0,0,...
  }

  @Test public void testIncrement2 () {
    int[] max = {200,50,10};
    Loops.Incrementer x = new Loops.Incrementer(max);
    assertEquals(3, x.degree());
    for (int i=0; i<10; i++) {
      for (int j=0; j<50; j++) {
        for (int k=0; k<200; k++) {
          assertEquals(i, x.indexes[2]);
          assertEquals(j, x.indexes[1]);
          assertEquals(k, x.indexes[0]);
          boolean overflow = x.incrementIndexVector();
          assertEquals(i==9 && j==49 && k==199, overflow);
        }
      }
    }
    assertEquals("Incrementer[0, 0, 0]", x.toString());
    assertEquals(200, x.maxAt(0));
    assertEquals(50, x.maxAt(1));
    max[2] = 42;
    assertEquals(42, x.maxAt(2));

    max = new int[16];
    Arrays.fill(max, 2);
    x = new Loops.Incrementer(max);
    assertEquals(16, x.degree());
    for (int i=0; i<0x1_00_00; i++) {
      int tmp = i;
      for (int j=0; j<16; j++) {
        assertEquals(tmp & 1, x.indexes[j]);
        tmp = tmp >> 1;
      }
      boolean overflow = x.incrementIndexVector();// 0..1
      assertEquals(i==0xFF_FF, overflow);
    }

    max = new int[1000];
    Arrays.fill(max, 1);
    x = new Loops.Incrementer(max);
    assertEquals(1000, x.degree());
    assertTrue(x.incrementIndexVector());
  }


  @Test public void testForLoop () {
    int[] arr = new int[11];
    Loops.forLoop(arr.length, (i)->{arr[i]=i;});
    assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]", Arrays.toString(arr));

    List<String> list = new ArrayList<>(100);

    Predicate<Loops.Incrementer> gen = (inc)->{
      list.add(inc.indexAt(1)+"*"+inc.indexAt(0)+"="+inc.indexAt(1)*inc.indexAt(0));
      return list.size()>=100;
    };

    Loops.Incrementer x = new Loops.Incrementer(2, 10);
    assertTrue(x.forLoop(gen));
    for (int i=0; i<10; i++) {
      for (int j=0; j<10; j++) {
        assertEquals(i+"*"+j+"="+i*j, list.get(i*10+j));
      }
    }

    list.clear();
    x = new Loops.Incrementer(new int[]{10,10});
    assertTrue(x.forLoop(gen));
    for (int i=0; i<10; i++) {
      for (int j=0; j<10; j++) {
        assertEquals(i+"*"+j+"="+i*j, list.get(i*10+j));
      }
    }
  }


}