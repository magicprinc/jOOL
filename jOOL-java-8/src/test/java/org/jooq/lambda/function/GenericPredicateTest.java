package org.jooq.lambda.function;

import org.jooq.lambda.Loops;
import org.jooq.lambda.Wrap;
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
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import static org.junit.Assert.*;

public class GenericPredicateTest {

  @SuppressWarnings("ConfusingArgumentToVarargsMethod")
  @Test public void dynamic () throws ClassNotFoundException {
    assertTrue(GenericPredicate.testDynamicPredicateVarArgs((Predicate0)()->true, null));
    assertFalse(GenericPredicate.testDynamicPredicateVarArgs((Predicate0)()->false));

    assertTrue(GenericPredicate.testDynamicArgCntToPredicate((Predicate0)()->true, null));
    assertFalse(GenericPredicate.testDynamicArgCntToPredicate((Predicate0)()->false));

    for (int i=0; i<=16; i++) {
      final int len = i;
      String ifaceClassName = "org.jooq.lambda.function.Predicate"+i;
      Class<GenericPredicate> ifaceClass = Wrap.castUnsafe(Class.forName(ifaceClassName));
      GenericPredicate p = (GenericPredicate) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
          new Class<?>[]{ifaceClass}, (proxy, method, args) -> {
            assertEquals("test", method.getName());

            if (len == 0) {
              assertNull(args);
            } else {
              assertEquals(len, args.length);
              for (int j = 0; j < len; j++) {
                assertEquals(j + 11, args[j]);
              }
            }
            return len % 2 == 0;
          });

      Object[] args = new Integer[len];
      for (int j=0; j<len; j++) {
        args[j] = j+11;
      }

      // [TYPE] real object type is used with so many args as necessary
      boolean pv = GenericPredicate.testDynamicPredicateVarArgs(p, args);
      assertEquals(pv, len % 2 == 0);

      // [ARGS] args.length tells us which PredicateX to use
      pv = GenericPredicate.testDynamicArgCntToPredicate(p, args);
      assertEquals(pv, len % 2 == 0);

      // [TYPE] only first "len" (0..15) array elements are used anyway
      args = new Integer[42];
      for (int j=0; j<len; j++) {
        args[j] = j+11;
      }
      pv = GenericPredicate.testDynamicPredicateVarArgs(p, args);
      assertEquals(pv, len % 2 == 0);
    }//f
  }


  @Test(expected = IllegalArgumentException.class)
  public void unknownType () {
    GenericPredicate.testDynamicPredicateVarArgs(new GenericPredicate() {});
    fail("unreachable");
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownSize () {
    GenericPredicate.testDynamicArgCntToPredicate(Wrap.P1_LOG_WARN_FALSE, new Object[42]);
    fail("unreachable");
  }


  @Test(expected = ClassCastException.class)
  public void classCastExceptionSize_AkaSizeAndTypeMismatch () {
    GenericPredicate.testDynamicArgCntToPredicate(Wrap.P1_LOG_WARN_FALSE, new Object[9]);
    fail("unreachable");
  }


  @Test(expected = ClassCastException.class)
  public void classCastExceptionArguments1 () {
    GenericPredicate.testDynamicPredicateVarArgs(Wrap.P1_LOG_WARN_FALSE, "anything", "Throwable!");
    //^ java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Throwable
    // @ return ((Predicate2) masterPredicateToSatisfy).test(args[0],args[1]);
    fail("unreachable");
  }

  @Test(expected = ClassCastException.class)
  public void classCastExceptionArguments2 () {
    GenericPredicate.testDynamicArgCntToPredicate(Wrap.P1_LOG_WARN_FALSE, "anything", "Throwable!");
    // java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Throwable @ case  2: return ((Predicate2)
    fail("unreachable");
  }


  @Test public void testPredicate0 () {
    Predicate0 p = () -> true;

    assertTrue(p.test());
    assertTrue(p.test(new Tuple0()));
    assertTrue(p.get());
    assertTrue(p.call());
    assertTrue(p.apply());
    assertSame(p, p.toSupplier());

    p = Predicate0.from(()->false);
    assertFalse(p.call());
  }


  static boolean logic (Integer... a) {
    int prev = 1;
    for (int i : a) {
      assertTrue("prev < i: "+prev+" ? "+i, prev < i);
      prev = i;
    }
    return a.length % 2 == 0;
  }

  @SuppressWarnings("unchecked")
  static <T extends Tuple> T tuple (int size) {
    try {
      final Class<?> cls = Class.forName("org.jooq.lambda.tuple.Tuple" + size);

      final Object[] args = new Object[size];
      Loops.forLoop(size, (i) -> args[i] = i + 10);

      for (Constructor<?> ctor : cls.getConstructors()) {
        if (ctor.getParameterCount() == size) {

          T tr = (T) ctor.newInstance(args);
          T tw = Wrap.tuple(args);

          assertEquals(tr, tw);
          return tr;
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Can't create Tuple"+size, e);
    }
    throw new IllegalArgumentException("Wrong size="+size+". Can't create Tuple"+size);
  }

  @Test public void testPredicate1 () {
    Predicate1<Integer> p = Predicate1.from(GenericPredicateTest::logic);
    assertSame(p, p.toPredicate());
    assertFalse(p.test(10));// arg.len==1→false
    assertFalse(p.applyPartially(10).test());
    Tuple1<Integer> tuple = tuple(1);
    assertFalse(p.test(tuple));
    assertFalse(p.toFunction().apply(tuple));
    assertFalse(p.applyPartially(tuple).test());

    p = Predicate1.fromFunction((i)->true);
    assertTrue(p.test(0));
  }


  @Test public void testLogic1 () {
    Predicate16<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
        Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;

    assertTrue(p.test(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25));
  }
  @Test(expected = AssertionError.class)
  public void testLogic2 () {
    Predicate16<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
        Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    assertTrue(p.test(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25));

    p.test(0,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25);
    fail("unreachable: ^^^ 0 < 1");
  }
  @Test(expected = AssertionError.class)
  public void testLogic3 () {
    Predicate16<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
        Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    assertTrue(p.test(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25));

    p.test(10,11,12,13,14,15,16,17,18,19,20, 22,21, 23,24,25);
    fail("unreachable: ^^^ AssertionError: prev < i: 22 ? 21");
  }


  @Test public void testPredicate2 () {
    Predicate2<Integer,Integer> p = Predicate2.from(GenericPredicateTest::logic);
    assertSame(p, p.toBiPredicate());
    assertTrue(p.test(10,11));
    assertTrue(p.applyPartially(10).test(11));
    assertTrue(p.applyPartially(10, 11).test());

    Tuple2<Integer,Integer> tuple = tuple(2);
    assertTrue(p.test(tuple));
    assertTrue(p.toFunction().apply(tuple));
    assertTrue(p.applyPartially(tuple).test());

    Tuple1<Integer> tuple1 = tuple(1);
    assertTrue(p.applyPartially(tuple1).test(11));

    p = Predicate2.fromFunction((a,b)->false);
    assertFalse(p.test(10,11));
  }


  @Test public void testPredicate3 () {
    Predicate3<Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple3<Integer,Integer,Integer> tuple = tuple(3);
    // 3 % 2 != 0
    assertFalse(p.toFunction().apply(tuple));
    assertFalse(p.test(tuple));
    assertFalse(p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertFalse(p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertFalse(p.applyPartially(tuple.limit3()).test(tuple.skip3()));


    assertFalse(p.test(10,11,12));
    assertFalse(p.applyPartially(10).test(11,12));
    assertFalse(p.applyPartially(10, 11).test(12));
    assertFalse(p.applyPartially(10, 11, 12).test());
  }

  @Test public void testPredicate4 () {
    Predicate4<Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple4<Integer,Integer,Integer,Integer> tuple = tuple(4);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));

    assertEquals(pv, p.test(10,11,12,13));
    assertEquals(pv, p.applyPartially(10).test(11,12,13));
    assertEquals(pv, p.applyPartially(10,11).test(12,13));
    assertEquals(pv, p.applyPartially(10,11,12).test(13));
    assertEquals(pv, p.applyPartially(10,11,12,13).test());
  }

  @Test public void testPredicate5 () {
    Predicate5<Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple5<Integer,Integer,Integer,Integer,Integer> tuple = tuple(5);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));

    assertEquals(pv, p.test(10,11,12,13,14));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test());
  }

  @Test public void testPredicate6 () {
    Predicate6<Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple6<Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(6);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));

    assertEquals(pv, p.test(10,11,12,13,14,15));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test());
  }

  @Test public void testPredicate7 () {
    Predicate7<Integer,Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple7<Integer,Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(7);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test());
  }

  @Test public void testPredicate8 () {
    Predicate8<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple8<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(8);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test());
  }

  @Test public void testPredicate9 () {
    Predicate9<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple9<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(9);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test());
  }

  @Test public void testPredicate10 () {
    Predicate10<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple10<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(10);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test());
  }

  @Test public void testPredicate11 () {
    Predicate11<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple11<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(11);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));
    assertEquals(pv, p.applyPartially(tuple.limit11()).test(tuple.skip11()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19,20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test(20));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20).test());
  }

  @Test public void testPredicate12 () {
    Predicate12<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple12<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer> tuple = tuple(12);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));
    assertEquals(pv, p.applyPartially(tuple.limit11()).test(tuple.skip11()));
    assertEquals(pv, p.applyPartially(tuple.limit12()).test(tuple.skip12()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19,20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test(20,21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20).test(21));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21).test());
  }

  @Test public void testPredicate13 () {
    Predicate13<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer>
        p = GenericPredicateTest::logic;
    Tuple13<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer>
        tuple = tuple(13);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));
    assertEquals(pv, p.applyPartially(tuple.limit11()).test(tuple.skip11()));
    assertEquals(pv, p.applyPartially(tuple.limit12()).test(tuple.skip12()));
    assertEquals(pv, p.applyPartially(tuple.limit13()).test(tuple.skip13()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19,20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test(20,21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20).test(21,22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21).test(22));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22).test());
  }

  @Test public void testPredicate14 () {
    Predicate14<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer>
        p = GenericPredicateTest::logic;
    Tuple14<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer>
        tuple = tuple(14);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));
    assertEquals(pv, p.applyPartially(tuple.limit11()).test(tuple.skip11()));
    assertEquals(pv, p.applyPartially(tuple.limit12()).test(tuple.skip12()));
    assertEquals(pv, p.applyPartially(tuple.limit13()).test(tuple.skip13()));
    assertEquals(pv, p.applyPartially(tuple.limit14()).test(tuple.skip14()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19,20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test(20,21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20).test(21,22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21).test(22,23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22).test(23));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22,23).test());
  }

  @Test public void testPredicate15 () {
    Predicate15<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
        Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple15<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
        Integer,Integer,Integer> tuple = tuple(15);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));
    assertEquals(pv, p.applyPartially(tuple.limit11()).test(tuple.skip11()));
    assertEquals(pv, p.applyPartially(tuple.limit12()).test(tuple.skip12()));
    assertEquals(pv, p.applyPartially(tuple.limit13()).test(tuple.skip13()));
    assertEquals(pv, p.applyPartially(tuple.limit14()).test(tuple.skip14()));
    assertEquals(pv, p.applyPartially(tuple.limit15()).test(tuple.skip15()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19,20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test(20,21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20).test(21,22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21).test(22,23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22).test(23,24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22,23).test(24));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24).test());
  }

  @Test public void testPredicate16 () {
    Predicate16<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
        Integer,Integer,Integer,Integer> p = GenericPredicateTest::logic;
    Tuple16<Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,Integer,
            Integer,Integer,Integer,Integer> tuple = tuple(16);
    boolean pv = tuple.degree() % 2 == 0;

    assertEquals(pv, p.toFunction().apply(tuple));
    assertEquals(pv, p.test(tuple));
    assertEquals(pv, p.applyPartially(tuple.limit1()).test(tuple.skip1()));
    assertEquals(pv, p.applyPartially(tuple.limit2()).test(tuple.skip2()));
    assertEquals(pv, p.applyPartially(tuple.limit3()).test(tuple.skip3()));
    assertEquals(pv, p.applyPartially(tuple.limit4()).test(tuple.skip4()));
    assertEquals(pv, p.applyPartially(tuple.limit5()).test(tuple.skip5()));
    assertEquals(pv, p.applyPartially(tuple.limit6()).test(tuple.skip6()));
    assertEquals(pv, p.applyPartially(tuple.limit7()).test(tuple.skip7()));
    assertEquals(pv, p.applyPartially(tuple.limit8()).test(tuple.skip8()));
    assertEquals(pv, p.applyPartially(tuple.limit9()).test(tuple.skip9()));
    assertEquals(pv, p.applyPartially(tuple.limit10()).test(tuple.skip10()));
    assertEquals(pv, p.applyPartially(tuple.limit11()).test(tuple.skip11()));
    assertEquals(pv, p.applyPartially(tuple.limit12()).test(tuple.skip12()));
    assertEquals(pv, p.applyPartially(tuple.limit13()).test(tuple.skip13()));
    assertEquals(pv, p.applyPartially(tuple.limit14()).test(tuple.skip14()));
    assertEquals(pv, p.applyPartially(tuple.limit15()).test(tuple.skip15()));
    assertEquals(pv, p.applyPartially(tuple.limit16()).test(tuple.skip16()));

    assertEquals(pv, p.test(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10).test(11,12,13,14,15,16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11).test(12,13,14,15,16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12).test(13,14,15,16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13).test(14,15,16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14).test(15,16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15).test(16,17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16).test(17,18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17).test(18,19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18).test(19,20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19).test(20,21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20).test(21,22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21).test(22,23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22).test(23,24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22,23).test(24,25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24).test(25));
    assertEquals(pv, p.applyPartially(10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25).test());
  }


  Object[] gen (int size) {
    Integer[] a = new Integer[size];
    Loops.forLoop(size, i->a[i]=i+1);
    return a;
  }
  String str (Object[] arr) {
    return Arrays.toString(arr).replace('[','(').replace(']', ')');
  }
  @Test
  public void wrapTuple () {
    assertEquals("()", Wrap.tuple().toString());
    assertTrue(Wrap.tuple() instanceof Tuple0);
    assertEquals("()", Wrap.tuple(null).toString());
    assertTrue(Wrap.tuple(null) instanceof Tuple0);
    assertEquals("()", Wrap.tuple(gen(0)).toString());
    assertEquals("(1)", Wrap.tuple(gen(1)).toString());
    assertEquals("(1, 2)", Wrap.tuple(gen(2)).toString());
    assertEquals("(1, 2, 3)", Wrap.tuple(gen(3)).toString());
    assertEquals("(1, 2, 3, 4)", Wrap.tuple(gen(4)).toString());
    assertEquals("(1, 2, 3, 4, 5)", Wrap.tuple(gen(5)).toString());
    assertEquals("(1, 2, 3, 4, 5, 6)", Wrap.tuple(gen(6)).toString());
    assertEquals(str(gen(7)), Wrap.tuple(gen(7)).toString());
    assertEquals(str(gen(8)), Wrap.tuple(gen(8)).toString());
    assertEquals(str(gen(9)), Wrap.tuple(gen(9)).toString());
    assertEquals(str(gen(10)), Wrap.tuple(gen(10)).toString());
    assertEquals(str(gen(11)), Wrap.tuple(gen(11)).toString());
    assertEquals(str(gen(12)), Wrap.tuple(gen(12)).toString());
    assertEquals(str(gen(13)), Wrap.tuple(gen(13)).toString());
    assertEquals(str(gen(14)), Wrap.tuple(gen(14)).toString());
    assertEquals(str(gen(15)), Wrap.tuple(gen(15)).toString());
    assertEquals(str(gen(16)), Wrap.tuple(gen(16)).toString());
    assertTrue(Wrap.tuple(gen(16)) instanceof Tuple16);

    try {
      Wrap.tuple(gen(17));
      fail("unreachable");
    } catch (IllegalArgumentException e) {
      assertEquals("java.lang.IllegalArgumentException: Unknown Tuple degree: 17 [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]", e.toString());
    }
  }
}