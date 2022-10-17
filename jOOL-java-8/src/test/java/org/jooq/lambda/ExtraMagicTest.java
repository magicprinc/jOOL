package org.jooq.lambda;

import org.jooq.lambda.function.Consumer3;
import org.junit.Test;

import java.io.IOError;
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import static org.jooq.lambda.LambdaLogFacade.printToStdErr;
import static org.jooq.lambda.Loops.loop;
import static org.junit.Assert.*;

/*
https://www.jmix.io/cuba-blog/think-twice-before-using-reflection/

https://github.com/cuba-rnd/entity-lambda-accessors-benchmark/blob/master/src/jmh/java/utils/MethodHandleCache.java

https://www.mail-archive.com/mlvm-dev@openjdk.java.net/msg06747.html

https://www.optaplanner.org/blog/2018/01/09/JavaReflectionButMuchFaster.html

https://wttech.blog/blog/2020/method-handles-and-lambda-metafactory/

https://forum.image.sc/t/performance-profiling-ways-of-invoking-a-method-dynamically/27471

!!!
private static final System.Logger log = System.getLogger("c.f.b.DefaultLogger");
+
clsLoader = Thread.currentThread().getContextClassLoader();
clsLoader = Wrap.class.getClassLoader();
clsLoader = ClassLoader.getPlatformClassLoader();
clsLoader = ClassLoader.getSystemClassLoader();
 */
public class ExtraMagicTest {
  static volatile Throwable FAILED;
  static final Method winner;
  static {
    Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) ->
      {
        FAILED = e;
        loop(9, ()-> System.err.println("{{{{{ DefaultUncaughtExceptionHandler @ "+t));
        e.printStackTrace();
        loop(9, ()-> System.err.println("}}}}} DefaultUncaughtExceptionHandler @ "+t));
      });

    //1. on/off → lambdaLogFacadeLoggerTest
    System.setProperty("org.jooq.logger", "org.jooq.lambda.ExtraMagicTest$LoggerOverride");
    final Method winner0 = LambdaLogFacade.findLoggerFactoryGetLogger("org.jooq.lambda.ExtraMagicTest$LoggerOverride");

    //2. slf4j
//    final Method winner0 = LambdaLogFacade.findLoggerFactoryGetLogger("org.slf4j.LoggerFactory");

    //3. log4j
//    System.setProperty("org.jooq.logger.0", "-");
//    final Method winner0 = LambdaLogFacade.findLoggerFactoryGetLogger("org.apache.logging.log4j.LogManager");
//    //4. ×JUL: no slf4j/log4j
//    System.setProperty("org.jooq.logger.0", "-");
//    System.setProperty("org.jooq.logger.1", "-");
//    final Method winner0 = null;

    if (Objects.equals(winner0, LambdaLogFacade.staticLoggerFactoryGetLoggerMethod)) {
      // good! We are the first UnitTest, we have initialized LambdaLogFacade with our wishes
      winner = winner0;
    } else if (LambdaLogFacade.staticLoggerFactoryGetLoggerMethod == null) {
      printToStdErr(ExtraMagicTest.class,
        "Other UnitTest was faster and have already initialized LambdaLogFacade with null (slf4j and log4j were not found)", null);
      winner = null;
    } else {
      printToStdErr(ExtraMagicTest.class,
        "Other UnitTest was faster and have already initialized LambdaLogFacade with "+LambdaLogFacade.staticLoggerFactoryGetLoggerMethod, null);
      winner = LambdaLogFacade.staticLoggerFactoryGetLoggerMethod;
    }
  }

  @Test(expected = IOError.class)
  public void andThen () {
    Error e1 = new Error("test1: → ERROR is OK here!!!");
    Unchecked.PRINT_STACK_TRACE.accept(e1);

    IOError e2 = new IOError(e1);
    Consumer<Throwable> c = Unchecked.SILENT_IGNORE
        .andThen((t) -> fail("unreachable. But: " + t));
    c.accept(e2);

    fail("unreachable");
  }


  @Test public void modulesHackJUL () {
    Object jul = getActualLogger("java.util.logging.Logger", ExtraMagicTest.class);
    assertEquals("class java.util.logging.Logger", jul.getClass().toString());
    assertTrue(jul.toString().startsWith("java.util.logging.Logger@"));
  }

  @Test public void modulesHackSlf4j () {
    Object slf4j = getActualLogger("org.slf4j.LoggerFactory", ExtraMagicTest.class);
    assertEquals("class org.slf4j.simple.SimpleLogger", slf4j.getClass().toString());// class org.slf4j.helpers.NOPLogger
    assertTrue(slf4j.toString().startsWith("org.slf4j.simple.SimpleLogger@"));// org.slf4j.helpers.NOPLogger(NOP)
  }

  @Test public void modulesHackLog4j () {
    Object log4j = getActualLogger("org.apache.logging.log4j.LogManager", ExtraMagicTest.class);
    assertEquals("class org.apache.logging.log4j.core.Logger", log4j.getClass().toString());
    assertTrue(log4j.toString().startsWith("org.jooq.lambda.ExtraMagicTest:ERROR in "));
  }

  public static class FakeLogger extends LambdaLogFacade implements LongSupplier, Runnable {
    public FakeLogger () {
      super(null);
    }

    final AtomicLong cnt = new AtomicLong();

    public static FakeLogger getLogger (String notUsed) {
      return new FakeLogger();
    }
    @Override public void warn (String msg, Throwable t) {
      assertNotNull(t);
      cnt.incrementAndGet();
      if (cnt.get() == MAX_LOOP) {
        new Exception("[INFO] Almost there! "+msg+"\t\t"+this).printStackTrace();
      }
    }
    @Override public long getAsLong () {
      return cnt.get();
    }
    @Override public void run () {}
  }

  static /*NOT final!*/ Throwable LOG_EXCEPTION = new Exception("fake");//MethodHandle.invokeExact check type of variable (not ref object)
  static final long MAX_LOOP = 10_000_000;
  static final long MAX_LOOP_WARM = 10_050_000;

  @Test public void speedDirect () {
    FakeLogger fl = new FakeLogger() {
      @Override public void run () {
        warn("message-to-log: Direct", LOG_EXCEPTION);
      }
    };
    String st = Loops.loopMeasuredWarm(MAX_LOOP, fl).toString();
    System.out.println("*** speedDirect →\t\t"+st);
    assertEquals(MAX_LOOP_WARM, fl.getAsLong());
  }

  @Test public void speedLambda () {
    FakeLogger fl = new FakeLogger();

    String st = Loops.loopMeasuredWarm(MAX_LOOP, Wrap.runnable(() ->
        fl.warn("message-to-log: Lambda", LOG_EXCEPTION))).toString();
    System.out.println("*** speedLambda →\t\t"+st);
    assertEquals(MAX_LOOP_WARM, fl.getAsLong());
  }
  @Test public void speedLambdaMethodRef () {
    FakeLogger fl = new FakeLogger(){
      @Override public void run () {
        warn("message-to-log: LambdaMethodRef", LOG_EXCEPTION);
      }
    };

    String st = Loops.loopMeasuredWarm(MAX_LOOP, Wrap.runnable(fl::run)).toString();
    System.out.println("*** speedLambdaMethodRef →\t\t"+st);
    assertEquals(MAX_LOOP_WARM, fl.getAsLong());
  }

  private static final MethodHandle staticMethodHandle;
  static {// Static MethodHandle setup ^
    try {
      Object fl = getActualLogger("org.jooq.lambda.ExtraMagicTest$FakeLogger", ExtraMagicTest.class);

      MethodHandles.Lookup lookup = MethodHandles.lookup();
      //1. method → MethodHandle
      //m = LambdaLogFacade.getWarnMethod(fl);
      //staticMethodHandle = lookup.unreflect(m);
      //2. .asType for invokeExact (vs invoke)
      staticMethodHandle = lookup.findVirtual(fl.getClass(), "warn",
          MethodType.methodType(Void.TYPE, String.class, Throwable.class))
          .asType(MethodType.methodType(Void.TYPE, LongSupplier.class/*this*/,
              /*args:*/ String.class, Throwable.class));
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test public void speedStaticMethodHandle () throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> cls = Class.forName("org.jooq.lambda.ExtraMagicTest$FakeLogger");
    final LongSupplier fl = (LongSupplier) cls.getConstructor().newInstance();

    String st = Loops.loopMeasuredWarm(MAX_LOOP, Unchecked.runnable(() -> {
      staticMethodHandle.invokeExact(fl, "message-to-log: MH", LOG_EXCEPTION);//vs invoke (ivoke is slower, but w/o .asType↑)
      //invokeExact→java.lang.invoke.WrongMethodTypeException: expected (FakeLogger,String,Throwable)void but found (LongSupplier,String,Exception)void
    })).toString();
    System.out.println("*** speedStaticMethodHandle →\t\t"+st);
    assertEquals(MAX_LOOP_WARM, fl.getAsLong());
  }


  private static final Method staticMethod;
  static {
    Object fl = getActualLogger("org.jooq.lambda.ExtraMagicTest$FakeLogger", ExtraMagicTest.class);
    staticMethod = LambdaLogFacade.getWarnMethod(fl);
  }
  @Test public void speedStaticReflection () throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
    Class<?> cls = Class.forName("org.jooq.lambda.ExtraMagicTest$FakeLogger");
    final LongSupplier fl = (LongSupplier) cls.getConstructor().newInstance();

    String st = Loops.loopMeasuredWarm(MAX_LOOP, Unchecked.runnable(() ->
        staticMethod.invoke(fl, "message-to-log: Reflection", LOG_EXCEPTION))).toString();
    System.out.println("*** speedStaticReflection →\t\t"+st);
    assertEquals(MAX_LOOP_WARM, fl.getAsLong());
  }


  private static final Consumer3<Object,String,Throwable> staticLambda;
  static {// Static Lambda setup ^
    try {
      Object fl = getActualLogger("org.jooq.lambda.ExtraMagicTest$FakeLogger", ExtraMagicTest.class);

      MethodHandles.Lookup lookup = MethodHandles.lookup();
      //1. method → MethodHandle
      Method m = LambdaLogFacade.getWarnMethod(fl);
      MethodHandle mhLogWarn = lookup.unreflect(m);

      MethodType genericMethodType = MethodType.methodType(Void.TYPE, fl.getClass()/*logger*/, String.class, Throwable.class);

      CallSite callSite = LambdaMetafactory.metafactory(
          lookup,// method handle lookup
          "accept",//Interface Method Name: name of the method defined in the target functional interface: Consumer::accept

          // type to be implemented and captured objects
          // e.g. String instance to be trimmed is captured: MethodType.methodType(Supplier.class, String.class)
          MethodType.methodType(Consumer3.class),

          // type erasure, Consumer3 will accept 3 Object NOT Void.TYPE, Object.class/*logger*/, String.class, Throwable.class
          MethodType.methodType(Void.TYPE, Object.class/*logger*/, Object.class, Object.class),

          mhLogWarn,// method handle to transform

          // Consumer3 method real signature (reified)
          // e.g. Supplier=trim accepts no parameters and returns String: MethodType.methodType(String.class)
          genericMethodType);

      //noinspection unchecked
      staticLambda = (Consumer3<Object,String,Throwable>) callSite.getTarget()
          /*.bindTo(contextObject e.g. "str")*/
          .invokeExact();// or invoke

    } catch (Throwable invokeExactEx) {
      invokeExactEx.printStackTrace();
      throw new IllegalStateException(invokeExactEx);
    }
  }
  @Test public void speedStaticLambda () {
    Object fl = getActualLogger("org.jooq.lambda.ExtraMagicTest$FakeLogger", ExtraMagicTest.class);

    String st = Loops.loopMeasuredWarm(MAX_LOOP, Unchecked.runnable(() ->
        staticLambda.accept(fl, "message-to-log: StaticLambda", LOG_EXCEPTION))).toString();
    System.out.println("*** speedStaticLambda →\t\t"+st);
    assertEquals(MAX_LOOP_WARM, ((LongSupplier) fl).getAsLong());
  }

  /** Get slf4j.Logger, JUL Logger, or log4j Logger */
  static Object getActualLogger (String loggerFactoryClassName, Class<?> clazzWhereLogger) {
    Method loggerFactoryGetLogger = LambdaLogFacade.findLoggerFactoryGetLogger(loggerFactoryClassName);
    if (loggerFactoryGetLogger == null) { return null;}// LoggerFactory not found in classpath
    return LambdaLogFacade.invokeGetLogger(loggerFactoryGetLogger, clazzWhereLogger);
  }


  @Test public void lambdaLogFacadeLoggerTest () throws InterruptedException {
    LambdaLogFacade log = LambdaLogFacade.getLogger(ExtraMagicTest.class);
    assertSame(ExtraMagicTest.class, log.loggerAppClassName);// ^^^

    printToStdErr(null, "#1 staticLoggerFactoryGetLoggerMethod= "+LambdaLogFacade.staticLoggerFactoryGetLoggerMethod, null);
    printToStdErr(null, "#2 staticWarnMethodHandle= "+LambdaLogFacade.staticWarnMethodHandle, null);
    assertEquals(winner, LambdaLogFacade.staticLoggerFactoryGetLoggerMethod);

    log.warn("lambdaLogFacade #1", null);
    log.warn("lambdaLogFacade #2", new Throwable("#2 Test Throwable"));
    log.warn("lambdaLogFacade #3", new Error("#3 Test Error"));
    String msg = "lambdaLogFacade #4";
    Exception t = new Exception("#4 Test Error");
    log.warn(msg, t);
    if (winner != null && winner.toString().contains("LoggerOverride")) {
      assertEquals(4, LoggerOverride.CNT.get());
      assertSame(msg, LoggerOverride.lastMessage);
      assertSame(t, LoggerOverride.lastThrowable);
    } else {
      System.err.println("WINNER is not LoggerOverride, so no checks");
    }

    final int THREADS = 100;
    final CountDownLatch startSignal = new CountDownLatch(1);
    final CountDownLatch doneSignal = new CountDownLatch(THREADS);
    loop(THREADS, (num) -> new Thread(()->{
      Wrap.run(startSignal::await);
      log.warn("Hello from thread "+num, new Exception("Thread "+num));
      loop(999, () -> log.warn("Hello from thread "+num, null));

      Wrap.run(doneSignal::countDown);
    }, "lambdaLogFacade-"+num).start());
    startSignal.countDown();// let's go!
    doneSignal.await();
    assertNull(FAILED);

    if (winner != null && winner.toString().contains("LoggerOverride")) {
      assertEquals(THREADS * 1000 + 4, LoggerOverride.CNT.get());
    }
  }

  public static class LoggerOverride extends LambdaLogFacade {
    public LoggerOverride () {
      super(null);
    }//new

    static final AtomicLong CNT = new AtomicLong();
    static String lastMessage;
    static Throwable lastThrowable;

    public static LoggerOverride getLogger (String notUsed) {
      return new LoggerOverride();
    }
    @Override public void warn (String msg, Throwable t) {
      CNT.incrementAndGet();
      lastMessage = msg;
      lastThrowable = t;
      if (t != null || msg.startsWith("lambda")) {
        System.out.println("LoggerOverride.warn >>> "+msg+"\t>>> "+t+" # "+Thread.currentThread());
      }
    }
  }//LoggerOverride

  @Test public void lambdaLogFacadeToStdErr () {
    LambdaLogFacade.LambdaLogFacadeStdErr log = new LambdaLogFacade.LambdaLogFacadeStdErr(ExtraMagicTest.class);
    assertSame(ExtraMagicTest.class, log.loggerAppClassName);// ^^^
    log.warn("Test message to System.err #1", null);
    log.warn("Test message to System.err #2 'with Throwable/Exception'",
        new IOException("#2 Test Throwable/Exception to System.err"));
  }

}