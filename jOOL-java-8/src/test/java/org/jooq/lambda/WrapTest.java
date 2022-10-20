package org.jooq.lambda;

import org.jooq.lambda.fi.util.concurrent.SafeCallable;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.jooq.lambda.fi.util.function.CheckedToDoubleBiFunction;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.jooq.lambda.Wrap.predicate;
import static org.jooq.lambda.Wrap.predicateDef;
import static org.junit.Assert.*;

@SuppressWarnings("divzero")
public class WrapTest {

  @Test(expected = ClassCastException.class)
  public void testCastUnsafe () {
    //java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')
    Integer x = Wrap.castUnsafe("1");
    System.err.println(x);
  }
  @Test(expected = ClassCastException.class)
  public void testCast () {
    //java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')
    Integer x = Wrap.cast(Integer.class, "1");
    System.err.println(x);
  }

  @Test public void castUnsafe () {
    Number o = 42L;
    long v = Wrap.castUnsafe(o);
    assertEquals(42, v);

    Object x = new ArrayList<String>();
    List<String> y = Wrap.castUnsafe(x);
    y.add("test");

    List<Integer> l1 = new ArrayList<>();
    List<String> l2 = Wrap.castUnsafe(l1);// dangerous! Don't use for this!
    l2.add("test");// → add "test" to List<Integer> l1
    Object boom1 = l1.get(0);
    Object boom2 = l2.get(0);
    assertSame(boom1, boom2);
    assertEquals("test", boom1);
  }
  @Test public void cast () {
    Number o = 42L;
    long v = Wrap.cast(Long.class, o);
    assertEquals(42, v);

    Object x = new ArrayList<String>();
    List<String> y = Wrap.cast(List.class, x);
    y.add("test");

    List<Integer> l1 = new ArrayList<>();
    List<String> l2 = Wrap.cast(List.class, l1);// dangerous! Don't use for this!
    l2.add("test");// → add "test" to List<Integer> l1
    Object boom1 = l1.get(0);
    Object boom2 = l2.get(0);
    assertSame(boom1, boom2);
    assertEquals("test", boom1);
    assertEquals(1, l1.size());
  }

  @Test(expected = NullPointerException.class)
  public void castUnsafeNullToPrimitive () {
    Boolean b1 = null;
    try {
      Boolean b2 = Wrap.castUnsafe(b1);
      assertNull(b2);
    } catch (Throwable e) {
      fail("(Boolean) null must be just fine "+e);
    }

    boolean b3 = Wrap.castUnsafe(b1);
    fail("^^^ NPE!");
  }
  @Test(expected = NullPointerException.class)
  public void castNullToPrimitive () {
    Boolean b1 = null;
    try {
      Boolean b2 = Wrap.cast(boolean.class, b1);
      assertNull(b2);
    } catch (Throwable e) {
      fail("(Boolean) null must be just fine "+e);
    }

    boolean b3 = Wrap.cast(boolean.class, b1);
    fail("^^^ NPE!");
  }



  @Test public void testCallable () throws Throwable {
    {
      Callable<String> callable = Wrap.callableDef(() -> {
        throw new IOException("log me");
      }, "");//with default value
      assertTrue(callable instanceof SafeCallable.Generated);
      assertEquals("", callable.call());
      assertEquals("", ((Supplier<?>) callable).get());
      ((SafeCallable<?>) callable).execute();
      assertNull(((SafeCallable<?>) callable).handleThrowable(null));
    }
    {
      SafeCallable<Either<String>> c2 = Wrap.callable(" xxx "::trim);
      assertTrue(c2 instanceof SafeCallable.Generated);
      assertEquals("Either(xxx)", c2.call().toString());
      assertEquals("xxx", c2.get().v1);
      assertEquals("xxx", c2.call().call());
      assertEquals("xxx", c2.execute().optValue().orElse(null));
      assertNull(c2.handleThrowable(null));

      System.err.println("*** NullPointerException with Either");

      c2 = Wrap.callable(() -> new String((char[]) null));
      assertTrue(c2 instanceof SafeCallable.Generated);
      assertTrue(c2.get().v2 instanceof NullPointerException);
      assertFalse(c2.call().optValue().isPresent());
      assertFalse(c2.execute().isPresent());
      assertTrue(c2.call().isFailure());
      assertTrue(c2.call().isEmpty());
      assertNull(c2.handleThrowable(null));
    }
    {
      SafeCallable.Generated<Integer> c = Wrap.cast(SafeCallable.Generated.class,
          Wrap.callable(WrapTest::divZero, (Throwable t)->
          {
            assertTrue(t.toString(), t instanceof ArithmeticException);
            return 42;
          }));
      assertEquals(42, c.get().intValue());
      assertEquals(42, c.call().intValue());
      assertEquals(42, c.execute().intValue());
      assertNull(c.handleThrowable(new Information("not used in Generated")));
    }
  }

  static class Information extends IOException {
    static final long serialVersionUID = 1;// make javac and error-prone happy
    public Information (Object message) { super(message.toString());}
    @Override public synchronized Throwable fillInStackTrace () { return this;}
  }


  @Test public void testPredicate () {
    Object[] arr = {1,2,3, null, "str", 3.14, true, (byte)42};

    long len = Stream.of(arr).filter(
        predicate((value) -> ((Integer) value) > 0)
      ).count();
    assertEquals(3, len);

    len = Seq.of(arr).filter(
        predicateDef((v)->Double.parseDouble(v.toString())>0, true)
    ).count();
    assertEquals(arr.length, len);

    len = Seq.of(arr).filter(
        predicateDef((v)->Double.parseDouble(v.toString())>0, false)
    ).count();
    assertEquals(5, len);// false only for 3: null, "str", true

    // NullPointerException: Cannot invoke "java.lang.Number.intValue()" because "v" is null
    // ClassCastException: String, Boolean cannot be cast to Number
    len = Seq.of(arr).filter(
        predicate((v)->((Number) v).intValue() > 0, (a,t)->
        {
          if (a == null) {
            assertTrue(t instanceof NullPointerException);
            return true;
          }
          return false;
        })
    ).count();
    assertEquals(6, len);// 5 +1 null
  }


  @Test public void eitherNull () throws Exception {
    Either<Object> zzz = Either.success(null);
    assertEquals(zzz, zzz);
    assertEquals(zzz, Either.empty());
    assertEquals(Either.success(null), Either.success(null));
    assertEquals(zzz, Optional.empty());

    Either<Integer> ei = Either.success(null);
    assertEquals(new Tuple2<Integer,Throwable>(null,null), ei);
    assertNull(ei.call());
    assertTrue(ei.isSuccess());
    assertFalse(ei.isFailure());
    assertFalse(ei.isPresent());
    assertTrue(ei.isEmpty());
    assertTrue(ei.isNull());
    assertEquals(0, ei.stream().count());
    assertEquals(31*31, ei.hashCode());
    assertEquals("Either.Empty", ei.toString());

    ei.ifPresent((value)->fail("null==empty==!present"));
    assertNull(ei.v2);
    assertEquals(Optional.empty(), ei.optThrowable());
    ei.throwIfFailure();

    AtomicInteger cnt = new AtomicInteger();
    ei.ifEmpty((Throwable t) -> {
      assertNull(t);
      cnt.incrementAndGet();
    });
    assertEquals(1, cnt.get());
    ei.ifNull(cnt::incrementAndGet);
    assertEquals(2, cnt.get());
    ei.ifFailure((t)->fail("null==success==!failure"));
    ei.ifSuccess((Integer v) -> {
      assertNull(v);
      cnt.incrementAndGet();
    });
    assertEquals(3, cnt.get());
    try {
      ei.throwIfEmpty();
      fail("unreachable: null value == !present");
    } catch (NoSuchElementException e) {
      assertEquals("java.util.NoSuchElementException: No value present", e.toString());
    }
    try {
      ei.throwIfNull();
      fail("unreachable: null value == !present");
    } catch (NoSuchElementException e) {
      assertEquals("java.util.NoSuchElementException: No value present", e.toString());
    }
  }

  @Test public void eitherValue () throws Exception {
    Either<Integer> zzz = Either.success(9);
    assertEquals(zzz, zzz);
    assertEquals(zzz, Either.success(9));
    assertEquals(zzz, 9);
    assertEquals(9, zzz.v1.intValue());
    assertEquals(9, zzz.optValue().get().intValue());
    assertNull(zzz.v2);

    Either<Integer> ei = Either.success(9);
    assertEquals(new Tuple2<Integer,Throwable>(9,null), ei);
    assertEquals(9, ei.call().intValue());
    assertTrue(ei.isSuccess());
    assertFalse(ei.isFailure());
    assertTrue(ei.isPresent());
    assertFalse(ei.isEmpty());
    assertFalse(ei.isNull());
    assertEquals(1, ei.stream().count());
    assertEquals(9, ei.stream().toArray()[0]);
    assertEquals((31+9)*31, ei.hashCode());
    assertEquals("Either(9)", ei.toString());
    assertNull(ei.v2);
    assertEquals(Optional.empty(), ei.optThrowable());
    ei.throwIfFailure().throwIfNull().throwIfEmpty();

    AtomicInteger cnt = new AtomicInteger();
    ei.ifPresent((Integer v)->cnt.incrementAndGet());
    assertEquals(1, cnt.get());

    ei.ifEmpty((Throwable t)->fail("!empty"));
    ei.ifNull(()->fail("!null"));
    ei.ifFailure((Throwable t)->fail("null==success==!failure"));
    ei.ifSuccess((Integer v) -> {
      assertEquals(9, v.intValue());
      cnt.incrementAndGet();
    });
    assertEquals(2, cnt.get());
    assertEquals(9, ei.call().intValue());
    assertEquals(9, ei.v1.intValue());
    assertEquals(9, ei.optValue().get().intValue());
    assertEquals(ei, Optional.of(9));
  }

  @Test public void eitherThrowable () throws Exception {
    Information ex = new Information("I am Either.Failure!");
    Either<Object> zzz = Either.failure(ex);
    assertEquals(zzz, zzz);
    assertNotEquals(zzz, null);
    assertEquals(zzz, Either.failure(ex));
    //assertEquals(Either.failure(ex), Optional.empty());
    assertEquals(Either.failure(ex), ex);

    Either<Integer> ei = Either.failure(ex);
    assertEquals(new Tuple2<Integer,Throwable>(null,ex), ei);

    try {
      ei.call();
      fail("unracahble: ^ throws ex");
    } catch (Information ex2) {
      assertSame(ex, ex2);
    }
    assertFalse(ei.isSuccess());
    assertTrue(ei.isFailure());
    assertFalse(ei.isPresent());
    assertTrue(ei.isEmpty());
    assertFalse(ei.isNull());
    assertEquals(0, ei.stream().count());
    assertTrue(ei.hashCode() != 0);
    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: I am Either.Failure!)", ei.toString());

    ei.ifPresent((value)->fail("null==empty==!present"));
    assertSame(ex, ei.v2);
    assertSame(ex, ei.optThrowable().get());
    assertNull(ei.v1);
    assertEquals(Optional.empty(), ei.optValue());
    try {
      ei.throwIfFailure();
      fail("unracahble: ^ throws");
    } catch (IllegalStateException x) {
      assertSame(ex, x.getCause());
      assertEquals("java.lang.IllegalStateException: Throwable instead of value", x.toString());
    }

    AtomicInteger cnt = new AtomicInteger();
    ei.ifEmpty((Throwable t) -> {
      assertSame(ex, t);
      cnt.incrementAndGet();
    });
    assertEquals(1, cnt.get());
    ei.ifNull(()->fail("failure != null"));
    ei.ifFailure((Throwable t) -> {
      assertSame(ex, t);
      cnt.incrementAndGet();
    });
    assertEquals(2, cnt.get());

    ei.ifSuccess((Integer v)->fail("failure!"));
    try {
      ei.throwIfEmpty();
      fail("unreachable: isFailure");
    } catch (IllegalStateException e) {
      assertEquals("java.lang.IllegalStateException: Throwable instead of value", e.toString());
    }
    ei.throwIfNull();
  }


  @Test public void eitherMix () {
    Either<Object> z = Either.success(null);
    z.consume((v,t)->{
      assertNull(v);
      assertNull(t);
    });
    assertEquals("Either.Empty", z.toString());
    assertNull(z.either());

    z = Either.success("");
    z.consume((v,t)->{
      assertEquals("", v);
      assertNull(t);
    });
    assertEquals("Either()", z.toString());
    assertEquals("", z.either());

    Information ex = new Information("{x}");
    z = Either.failure(ex);
    z.consume((v,t)->{
      assertNull(v);
      assertSame(ex, t);
    });
    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: {x})", z.toString());
    assertEquals(ex, z.either());
  }


  @Test public void directRunAndCall () {
    assertTrue(Wrap.run(()->{}));
    assertTrue(Wrap.runStd(()->{}));
    assertFalse(Wrap.run(()->{throw new Information("#1");}));
    assertFalse(Wrap.runStd(()->{throw new RuntimeException("#2");}));

    assertEquals(Either.empty(), Wrap.call(()->null));
    assertEquals(Either.empty(), Wrap.callStd(()->null));
    assertEquals(42, Wrap.call(()->42).v1.intValue());
    assertEquals(3.14, (Object) Wrap.callStd(()->3.14).v1);
    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: Hi!)",
        Wrap.call(()->{throw new Information("Hi!");}).toString());
    assertEquals("Either.Failure(java.lang.ArithmeticException: / by zero)",
        Wrap.callStd(()->1/0).toString());
  }


  @Test public void testRunnable () {
    Wrap.runnable(()->{}).run();

    AtomicInteger cnt = new AtomicInteger();
    Wrap.runnable(cnt::incrementAndGet).run();
    assertEquals(1, cnt.get());

    Wrap.runnable(null).run();
  }


  @Test public void safeThrows () {
    Exception e = new Exception("Checked!");
    IOException io = new IOException();
    InterruptedException ie = new InterruptedException();
    Wrap.throwIfError(e);
    Wrap.throwIfError(new RuntimeException());
    Wrap.throwIfError(io);
    Thread.interrupted();
    Wrap.throwIfError(ie);
    assertTrue(Thread.interrupted());

    Wrap.throwIfErrorOrRuntime(e);
    Wrap.throwIfErrorOrRuntime(io);
    Wrap.throwIfErrorOrRuntime(ie);
    assertTrue(Thread.interrupted());

    Wrap.throwIfErrorOrRuntimeOrIo(e);
    Wrap.throwIfErrorOrRuntimeOrIo(ie);
    assertTrue(Thread.interrupted());
    assertFalse(Thread.interrupted());

    Wrap.handleInterruptedException(ie);
    assertTrue(Thread.interrupted());
    assertFalse(Thread.interrupted());

    Wrap.handleInterruptedException((Throwable)ie);
    assertTrue(Thread.interrupted());
    assertFalse(Thread.interrupted());
  }


  @Test(expected = Error.class)
  public void throw1 () {
    Wrap.throwIfError(new Error());
  }

  @Test(expected = RuntimeException.class)
  public void throw2 () {
    Wrap.throwIfErrorOrRuntime(new RuntimeException());
  }

  @Test(expected = UncheckedIOException.class)
  public void throw3 () {
    Wrap.throwIfErrorOrRuntimeOrIo(new Information(1));
    // java.io.UncheckedIOException: org.jooq.lambda.WrapTest$Information: 1
  }


  @Test public void testPredicates () {
    assertTrue(Wrap.predicate((Boolean v)->v).test(true));
    assertFalse(Wrap.predicate((Boolean v)->v).test(false));
    assertFalse(Wrap.predicate((Boolean v)->v).test(null));

    assertTrue(Wrap.predicateDef((Boolean v)->v, true).test(true));
    assertFalse(Wrap.predicateDef((Boolean v)->v, true).test(false));
    assertTrue(Wrap.predicateDef((Boolean v)->v, true).test(null));

    assertTrue(Wrap.predicate((Boolean v)->v, (Boolean v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(true));
    assertFalse(Wrap.predicate((Boolean v)->v, (Boolean v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(false));
    assertTrue(Wrap.predicate((Boolean v)->v, (Boolean v,Throwable t)->{
      assertNull(v);
      assertTrue(t instanceof NullPointerException);
      return true;}).test(null));
  }

  @Test public void testBiPredicates () {
    assertTrue(Wrap.biPredicate((Byte x,Byte y)->x<y).test((byte)1,(byte)2));
    assertFalse(Wrap.biPredicate((Byte x,Byte y)->x<y).test((byte)1,(byte)1));
    assertFalse(Wrap.biPredicate((Byte x,Byte y)->x<y).test(null,(byte)2));

    assertTrue(Wrap.biPredicateDef((Byte x,Byte y)->x<y, true).test((byte)1,(byte)2));
    assertFalse(Wrap.biPredicateDef((Byte x,Byte y)->x<y, true).test((byte)1,(byte)1));
    assertTrue(Wrap.biPredicateDef((Byte x,Byte y)->x<y, true).test(null,(byte)2));

    assertTrue(Wrap.biPredicate((Byte x,Byte y)->x<y, (Byte x,Byte y,Throwable t)->{
      throw new AssertionError("unreachable");}).test((byte)1,(byte)2));
    assertFalse(Wrap.biPredicate((Byte x,Byte y)->x<y, (Byte x,Byte y,Throwable t)->{
      throw new AssertionError("unreachable");}).test((byte)1,(byte)1));
    assertTrue(Wrap.biPredicate((Byte x,Byte y)->x<y, (Byte x,Byte y,Throwable t)->{
      assertNull(x);
      assertEquals((byte)2,(Object)y);
      assertTrue(t instanceof NullPointerException);
      return true;}).test(null,(byte)2));
  }

  @Test public void testDoublePredicates () {
    assertTrue(Wrap.doublePredicate((double v)->v>0).test(1));
    assertFalse(Wrap.doublePredicate((double v)->v>0).test(-1));
    assertFalse(Wrap.doublePredicate((double v)->{throw new Exception();}).test(1));

    assertTrue(Wrap.doublePredicateDef((double v)->v>0, true).test(1));
    assertFalse(Wrap.doublePredicateDef((double v)->v>0, true).test(-1));
    assertTrue(Wrap.doublePredicateDef((double v)->{throw new Exception();}, true).test(0));

    assertTrue(Wrap.doublePredicate((double v)->v>0, (Double v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(1));
    assertFalse(Wrap.doublePredicate((double v)->v>1, (Double v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(0));
    assertTrue(Wrap.doublePredicate((double v)->{throw new IOException();}, (Double v,Throwable t)->{
      assertEquals(1, v, 0.01);
      assertTrue(t instanceof IOException);
      return true;}).test(1));
  }

  @Test public void testLongPredicates () {
    assertTrue(Wrap.longPredicate((long v)->v>0).test(1));
    assertFalse(Wrap.longPredicate((long v)->v>0).test(-1));
    assertFalse(Wrap.longPredicate((long v)->{throw new Exception();}).test(1));

    assertTrue(Wrap.longPredicateDef((long v)->v>0, true).test(1));
    assertFalse(Wrap.longPredicateDef((long v)->v>0, true).test(-1));
    assertTrue(Wrap.longPredicateDef((long v)->{throw new Exception();}, true).test(0));

    assertTrue(Wrap.longPredicate((long v)->v>0, (Long v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(1));
    assertFalse(Wrap.longPredicate((long v)->v>1, (Long v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(0));
    assertTrue(Wrap.longPredicate((long v)->{throw new IOException();}, (Long v,Throwable t)->{
      assertEquals(1, v.longValue());
      assertTrue(t instanceof IOException);
      return true;}).test(1));
  }

  @Test public void testIntPredicates () {
    assertTrue(Wrap.intPredicate((int v)->v>0).test(1));
    assertFalse(Wrap.intPredicate((int v)->v>0).test(-1));
    assertFalse(Wrap.intPredicate((int v)->{throw new Exception();}).test(1));

    assertTrue(Wrap.intPredicateDef((int v)->v>0, true).test(1));
    assertFalse(Wrap.intPredicateDef((int v)->v>0, true).test(-1));
    assertTrue(Wrap.intPredicateDef((int v)->{throw new Exception();}, true).test(0));

    assertTrue(Wrap.intPredicate((int v)->v>0, (Integer v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(1));
    assertFalse(Wrap.intPredicate((int v)->v>1, (Integer v,Throwable t)->{
      throw new AssertionError("unreachable");}).test(0));
    assertTrue(Wrap.intPredicate((int v)->{throw new IOException();}, (Integer v,Throwable t)->{
      assertEquals(1, v, 0.01);
      assertTrue(t instanceof IOException);
      return true;}).test(1));
  }


  @Test public void predicateWithLogHandler () {
    assertTrue(Wrap.predicate((v)->{throw new IOException();},
        (Object v, Throwable t)->{
          assertEquals(1, v);
          assertTrue(t instanceof IOException);
          return true;
        }).test(1));
    assertTrue(Wrap.predicate((v)->{throw new IOException();},
        Wrap.P1_LOG_WARN_TRUE
        ).test(1));
    assertTrue(Wrap.predicate((v)->{throw new IOException();},
        Wrap.P1_SILENT_IGNORE_ALL_TRUE
    ).test(1));
    assertFalse(Wrap.predicate((v)->{throw new IOException();},
        Wrap.P1_SILENT_IGNORE_ALL_FALSE
    ).test(1));
  }


  @Test public void testFunctions () {
    assertEquals("Either(9)", Wrap.function((i)->""+i).apply(9).toString());
    assertEquals("9", Wrap.function((i)->""+i, (i,t)->{
      assertEquals(9, i);
      assertNull(t);
      return null;
    }).apply(9));
    assertEquals("9", Wrap.functionDef((i)->""+i, "x").apply(9));

    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: f1.1)",
      Wrap.function((i)->{throw new Information("f1.1");}).apply(9).toString());
    assertEquals("H", Wrap.function((i)->{throw new Information("f1.2");}, (i,t)->{
      assertEquals(9, i);
      assertTrue(t instanceof Information);
      return "H";
    }).apply(9));
    assertEquals("x", Wrap.functionDef((i)->{throw new Information("f1.3");}, "x").apply(9));
    //
    CheckedBiFunction<Integer,Integer,String> cbif = (a,b)->""+a+b;
    assertEquals("Either(12)", Wrap.biFunction(cbif).apply(1,2).toString());
    assertEquals("12", Wrap.biFunction(cbif, (a,b,t)->{
      assertEquals((Integer)1, a);
      assertEquals((Integer)2, b);
      assertNull(t);
      return null;
    }).apply(1,2));
    assertEquals("12", Wrap.biFunctionDef(cbif, "x").apply(1,2));

    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: f2.1)",
      Wrap.biFunction((a,b)->{throw new Information("f2.1");}).apply(1,2).toString());
    assertEquals("H", Wrap.biFunction((a,b)->{throw new Information("f2.2");}, (a,b,t)->{
      assertEquals(1, a);
      assertEquals(2, b);
      assertTrue(t instanceof Information);
      return "H";
    }).apply(1,2));
    assertEquals("x", Wrap.biFunctionDef((a,b)->{throw new Information("f2.3");}, "x").apply(1,2));
    //
    assertEquals("Either(9.1)", Wrap.doubleFunction((d)->""+d).apply(9.1).toString());
    assertEquals("9.1", Wrap.doubleFunction((d)->""+d, (d,t)->{
      assertEquals(9.1, d, 0.01);
      assertNull(t);
      return null;
    }).apply(9.1));
    assertEquals("9.1", Wrap.doubleFunctionDef((d)->""+d, "x").apply(9.1));

    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: f1.1)",
      Wrap.doubleFunction((i)->{throw new Information("f1.1");}).apply(9.1).toString());
    assertEquals("H", Wrap.doubleFunction((d)->{throw new Information("f1.2");}, (d,t)->{
      assertEquals(9.1, d, 0.01);
      assertTrue(t instanceof Information);
      return "H";
    }).apply(9.1));
    assertEquals("x", Wrap.doubleFunctionDef((i)->{throw new Information("f1.3");}, "x").apply(9.1));

    // intFunction
    assertEquals("Either(9)", Wrap.intFunction((d)->""+d).apply(9).toString());
    assertEquals("9", Wrap.intFunction((d)->""+d, (d,t)->{
      assertEquals((Integer)9, d);
      assertNull(t);
      return null;
    }).apply(9));
    assertEquals("9", Wrap.intFunctionDef((d)->""+d, "x").apply(9));

    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: fI.1)",
      Wrap.intFunction((i)->{throw new Information("fI.1");}).apply(9).toString());
    assertEquals("H", Wrap.intFunction((d)->{throw new Information("fI.2");}, (d,t)->{
      assertEquals((Integer)9, d);
      assertTrue(t instanceof Information);
      return "H";
    }).apply(9));
    assertEquals("x", Wrap.intFunctionDef((i)->{throw new Information("fI.3");}, "x").apply(9));

    // longFunction
    assertEquals("Either(9)", Wrap.longFunction((d)->""+d).apply(9L).toString());
    assertEquals("9", Wrap.longFunction((d)->""+d, (d,t)->{
      assertEquals((Long)9L, d);
      assertNull(t);
      return null;
    }).apply(9L));
    assertEquals("9", Wrap.longFunctionDef((d)->""+d, "x").apply(9L));

    assertEquals("Either.Failure(org.jooq.lambda.WrapTest$Information: fL.1)",
      Wrap.longFunction((i)->{throw new Information("fL.1");}).apply(9L).toString());
    assertEquals("H", Wrap.longFunction((d)->{throw new Information("fL.2");}, (d,t)->{
      assertEquals((Long)9L, d);
      assertTrue(t instanceof Information);
      return "H";
    }).apply(9L));
    assertEquals("x", Wrap.longFunctionDef((i)->{throw new Information("fL.3");}, "x").apply(9L));
  }

  @Test public void testFunctions2 () {
    assertEquals(9, Wrap.doubleToIntFunction((d)->(int)d, (d,t)->{
      assertEquals(9.1, d, 0.01);
      assertNull(t);
      return null;
    }).applyAsInt(9.1));
    assertEquals(9, Wrap.doubleToIntFunctionDef((d)->(int)d, -1).applyAsInt(9.1));

    assertEquals(-5, Wrap.doubleToIntFunction((d)-> 1/0, (d,t)->{
      assertEquals(9.1, d, 0.01);
      assertTrue(t.toString(), t instanceof ArithmeticException);
      return -5;
    }).applyAsInt(9.1));
    assertEquals(-5, Wrap.doubleToIntFunctionDef((i)->{throw new Information("f1.3");}, -5).applyAsInt(9.1));
    //
    assertEquals(9L, Wrap.doubleToLongFunction((d)->(int)d, (d,t)->{
      assertEquals(9.1, d, 0.01);
      assertNull(t);
      return null;
    }).applyAsLong(9.1));
    assertEquals(9L, Wrap.doubleToLongFunctionDef((d)->(int)d, -1).applyAsLong(9.1));

    assertEquals(-5L, Wrap.doubleToLongFunction((d)-> 1/0, (d,t)->{
      assertEquals(9.1, d, 0.01);
      assertTrue(t.toString(), t instanceof ArithmeticException);
      return -5L;
    }).applyAsLong(9.1));
    assertEquals(-5L, Wrap.doubleToLongFunctionDef((i)->{throw new Information("f1.3");}, -5).applyAsLong(9.1));

    // intToDoubleFunction
    assertEquals(9.9, Wrap.intToDoubleFunction((d)->d*1.1, (d,t)->{
      assertEquals(9, d, 0.01);
      assertNull(t);
      return null;
    }).applyAsDouble(9), 0.01);
    assertEquals(9.9, Wrap.intToDoubleFunctionDef((d)->d*1.1,-1).applyAsDouble(9), 0.01);

    assertEquals(-5.1, Wrap.intToDoubleFunction((d)-> 1/0, (d,t)->{
      assertEquals((Integer)9, d);
      assertTrue(t.toString(), t instanceof ArithmeticException);
      return -5.1;
    }).applyAsDouble(9), 0.01);
    assertEquals(-5.1, Wrap.intToDoubleFunctionDef((i)->{throw new Information("f1.3");}, -5.1).applyAsDouble(9), 0.01);

    // intToLongFunction
    assertEquals(90L, Wrap.intToLongFunction((d)->d*10L, (d,t)->{
      assertEquals((Integer)9, d);
      assertNull(t);
      return null;
    }).applyAsLong(9));
    assertEquals(90L, Wrap.intToLongFunctionDef((d)->d*10L,-1L).applyAsLong(9));

    assertEquals(-5L, Wrap.intToLongFunction((d)-> 1/0, (d,t)->{
      assertEquals((Integer)9, d);
      assertTrue(t.toString(), t instanceof ArithmeticException);
      return -5L;
    }).applyAsLong(9));
    assertEquals(-5L, Wrap.intToLongFunctionDef((i)->{throw new Information("f1.3");}, -5L).applyAsLong(9));

    // longToDoubleFunction
    assertEquals(9.9, Wrap.longToDoubleFunction((d)->d*1.1, (d,t)->{
      assertEquals((Long)9L, d);
      assertNull(t);
      return -1.1;
    }).applyAsDouble(9L), 0.01);
    assertEquals(9.9, Wrap.longToDoubleFunctionDef((d)->d*1.1,-1.1).applyAsDouble(9L), 0.01);

    assertEquals(-5.1, Wrap.longToDoubleFunction((d)-> 1/0, (d,t)->{
      assertEquals((Long)9L, d);
      assertTrue(t.toString(), t instanceof ArithmeticException);
      return -5.1;
    }).applyAsDouble(9L), 0.01);
    assertEquals(-5.1, Wrap.longToDoubleFunctionDef((i)->1/0, -5.1).applyAsDouble(9L), 0.01);

    // longToIntFunction
    assertEquals(8, Wrap.longToIntFunction((d)->(int)d-1, (d,t)->{
      assertEquals((Long)9L, d);
      assertNull(t);
      return -5;
    }).applyAsInt(9L));
    assertEquals(8, Wrap.longToIntFunctionDef((d)->(int)d-1,-1).applyAsInt(9L));

    assertEquals(-5, Wrap.longToIntFunction((d)-> 1/0, (d,t)->{
      assertEquals((Long)9L, d);
      assertTrue(t.toString(), t instanceof ArithmeticException);
      return -5;
    }).applyAsInt(9L));
    assertEquals(-5, Wrap.longToIntFunctionDef((i)->divZero(), -5).applyAsInt(9L));
  }


  @Test
  public void testFunctionToPrimitive () {
    // toDoubleBiFunction
    CheckedToDoubleBiFunction<Integer,Integer> cbif = (a,b)->a*1.1+b;
    assertEquals(1.1+2, Wrap.toDoubleBiFunction(cbif, (a,b,t)->{
      assertEquals((Integer)1, a);
      assertEquals((Integer)2, b);
      assertNull(t);
      return null;
    }).applyAsDouble(1,2), 0.01);
    assertEquals(1.1+2, Wrap.toDoubleBiFunctionDef(cbif, -9.1).applyAsDouble(1,2), 0.01);

    assertEquals(-5.7, Wrap.toDoubleBiFunction((Integer a, Integer b)->1/0, (a,b,t)->{
      assertEquals((Integer)1, a);
      assertEquals((Integer)2, b);
      assertTrue(t instanceof ArithmeticException);
      return -5.7;
    }).applyAsDouble(1,2), 0.01);
    assertEquals(-1.5, Wrap.toDoubleBiFunctionDef((Integer a, Integer b)->{throw new Information("D");}, -1.5).applyAsDouble(1,2), 0.01);

    // toDoubleFunction
    assertEquals(9.1, Wrap.toDoubleFunction(Double::parseDouble, (v,t)->{
      assertEquals("9.1", v);
      assertNull(t);
      return null;
    }).applyAsDouble("9.1"), 0.01);
    assertEquals(9.1, Wrap.toDoubleFunctionDef(Double::parseDouble, -9.1).applyAsDouble("9.1"), 0.01);

    assertEquals(-5.7, Wrap.toDoubleFunction((v)->(int)v, (v,t)->{
      assertEquals("9.1", v);
      assertTrue(t.toString(), t instanceof ClassCastException);
      return -5.7;
    }).applyAsDouble("9.1"), 0.01);
    assertEquals(-1.5, Wrap.toDoubleFunctionDef((v)->{throw new Information("D");}, -1.5).applyAsDouble("9.1"), 0.01);

    // toIntBiFunction
    assertEquals(12, Wrap.toIntBiFunction((Long a, Long b)->(int)(a*10+b), (a,b,t)->{
      assertEquals((Long)1L, a);
      assertEquals((Long)2L, b);
      assertNull(t);
      return null;
    }).applyAsInt(1L,2L));
    assertEquals(12, Wrap.toIntBiFunctionDef((Long a, Long b)->(int)(a*10+b), -1).applyAsInt(1L,2L));

    assertEquals(-5, Wrap.toIntBiFunction((Integer a, Long b)->1/0, (a,b,t)->{
      assertEquals((Integer)1, a);
      assertEquals((Long)2L, b);
      assertTrue(t instanceof ArithmeticException);
      return -5;
    }).applyAsInt(1,2L));
    assertEquals(-1, Wrap.toIntBiFunctionDef((Integer a, Long b)->{throw new Information("D");}, -1).applyAsInt(1,2L));

    // toIntFunction
    assertEquals(10, Wrap.toIntFunction((Long v)->(int)(v*10), (v,t)->{
      assertEquals((Long)1L, v);
      assertNull(t);
      return null;
    }).applyAsInt(1L));
    assertEquals(10, Wrap.toIntFunctionDef((Long v)->(int)(v*10), -1).applyAsInt(1L));

    assertEquals(-5, Wrap.toIntFunction((Integer v)->1/0, (v,t)->{
      assertEquals((Integer)1, v);
      assertTrue(t instanceof ArithmeticException);
      return -5;
    }).applyAsInt(1));
    assertEquals(-1, Wrap.toIntFunctionDef((Integer a)->{throw new Information("D");}, -1).applyAsInt(1));

    // toLongBiFunction
    assertEquals(12L, Wrap.toLongBiFunction((Integer a, Byte b)->a*10L+b, (a,b,t)->{
      assertEquals((Integer)1, a);
      assertEquals((byte)2, b.byteValue());
      assertNull(t);
      return null;
    }).applyAsLong(1,(byte)2));
    assertEquals(12L, Wrap.toLongBiFunctionDef((Integer a, Short b)->a*10L+b, -1).applyAsLong(1,(short)2));

    assertEquals(-5L, Wrap.toLongBiFunction((Double a, Long b)->1/0, (a,b,t)->{
      assertEquals(1.1, a, 0.01);
      assertEquals((Long)2L, b);
      assertTrue(t instanceof ArithmeticException);
      return -5L;
    }).applyAsLong(1.1,2L));
    assertEquals(-1L, Wrap.toLongBiFunctionDef((Integer a, Long b)->{throw new Information("D");}, -1L).applyAsLong(1,2L));

    // toLongFunction
    assertEquals(10L, Wrap.toLongFunction(Double::longValue, (v,t)->{
      assertEquals(10.1, v, 0.01);
      assertNull(t);
      return null;
    }).applyAsLong(10.1));
    assertEquals(10L, Wrap.toLongFunctionDef((Integer v)->v*10L, -1).applyAsLong(1));

    assertEquals(-5L, Wrap.toLongFunction((Integer v)->1/0, (v,t)->{
      assertEquals((Integer)1, v);
      assertTrue(t instanceof ArithmeticException);
      return -5L;
    }).applyAsLong(1));
    assertEquals(-1L, Wrap.toLongFunctionDef((Integer a)->{throw new Information("D");}, -1L).applyAsLong(1));

  }


  static <T> T divZero () {
    return Wrap.castUnsafe(1/0);
  }
}