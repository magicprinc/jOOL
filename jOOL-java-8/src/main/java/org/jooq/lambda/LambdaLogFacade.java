package org.jooq.lambda;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 Lambda Factory - CallSite - static Method Handle

 1) slf4j
 2) log4j
 ×3) System.Logger - not used because of a compete different API
 ×4) JUL (java.util.logging.Logger) - no warn(msg,t) method
 5) System.err

 private static final System.Logger LOGGER = System.getLogger("c.f.b.DefaultLogger");
 org.slf4j:slf4j-jdk-platform-logging

 @author Andrej Fink
 */
abstract class LambdaLogFacade implements Thread.UncaughtExceptionHandler {
  static final MethodHandle staticWarnMethodHandle;
  static final Method staticLoggerFactoryGetLoggerMethod;


  public abstract void warn (String msg, Throwable t);


  /** LambdaLogFacade produces Loggers (subclasses of LambdaLogFacade).
   Similar to Slf4j: Logger log = LoggerFactory.getLogger(SomeAppClass.class) */
  public static LambdaLogFacade getLogger (Class<?> clazzWhereLogger) {
    if (staticWarnMethodHandle == null || staticLoggerFactoryGetLoggerMethod == null) {
      return new LambdaLogFacadeStdErr(clazzWhereLogger);
    }
    Object logger = invokeGetLogger(staticLoggerFactoryGetLoggerMethod, clazzWhereLogger);
    if (logger == null) {
      return new LambdaLogFacadeStdErr(clazzWhereLogger);
    }
    return new LambdaLogFacadeDelegate(clazzWhereLogger, logger);
  }


  @Override public void uncaughtException (Thread t, Throwable ex) {
    warn("UncaughtExceptionHandler in Thread: "+t, ex);
  }

// internals

  static {
    List<String> loggerFactoryClassNames = new ArrayList<>(11);
    addLoggerFacadeClassName(loggerFactoryClassNames, "org.jooq.logger", null);// can be overridden
    addLoggerFacadeClassName(loggerFactoryClassNames, "org.jooq.logger.0", "org.slf4j.LoggerFactory");
    addLoggerFacadeClassName(loggerFactoryClassNames, "org.jooq.logger.1", "org.apache.logging.log4j.LogManager");
    addLoggerFacadeClassName(loggerFactoryClassNames, "org.jooq.logger.2", null);// "java.util.logging.Logger"
    addLoggerFacadeClassName(loggerFactoryClassNames, "org.jooq.logger.3", null);

    MethodHandle mh = null;
    Method loggerFactoryGetLogger = null;

    for (String loggerFactoryClassName : loggerFactoryClassNames) {
      loggerFactoryGetLogger = findLoggerFactoryGetLogger(loggerFactoryClassName);
      if (loggerFactoryGetLogger == null) { continue;}// LoggerFactory not found in classpath

      Object tmpLogger4warnMethod = invokeGetLogger(loggerFactoryGetLogger, LambdaLogFacade.class);
      if (tmpLogger4warnMethod == null) { continue;}// this logging library isn't properly initialized?

      Method warnMethod = getWarnMethod(tmpLogger4warnMethod);
      if (warnMethod == null) { continue;}// ?!

      try {
        mh = MethodHandles.lookup().unreflect(warnMethod)
            .asType(MethodType.methodType(Void.TYPE/*void warn(*/, Object.class/*Logger*/,
            /*args: msg, t */ String.class, Throwable.class));
        break;// found!
      } catch (Throwable t) {
        printToStdErr(LambdaLogFacade.class,
            "Can't init logger "+tmpLogger4warnMethod+" :: "+warnMethod+" @ "+loggerFactoryGetLogger, t);
      }
    }//f slf4j, log4j, jul

    staticWarnMethodHandle = mh;
    staticLoggerFactoryGetLoggerMethod = loggerFactoryGetLogger;
  }

  private static void addLoggerFacadeClassName (List<String> dst, String key, String defaultClassName) {
    String s = System.getProperty(key);
    if (s == null) {
      s = System.getenv(key);
    }
    if (s == null) {
      s = defaultClassName;
    }
    if (s != null && s.length()>1) {
      dst.add(s);
    }
  }


  protected final Class<?> loggerAppClassName;

  protected LambdaLogFacade (Class<?> clazzWhereLogger) {
    loggerAppClassName = clazzWhereLogger;
  }//new

  public static void printToStdErr (Class<?> appLoggerName, String msg, Throwable t) {
    System.err.println(System.currentTimeMillis()+"\t[WARN]\t"+appLoggerName+"\t- "+ msg);
    if (t != null) {
      t.printStackTrace();
    }
  }

  static class LambdaLogFacadeStdErr extends LambdaLogFacade {
    LambdaLogFacadeStdErr (Class<?> clazzWhereLogger) { super(clazzWhereLogger);}//new

    @Override public void warn (String msg, Throwable t) {
      printToStdErr(this.loggerAppClassName, msg, t);
    }
  }//LambdaLogFacadeStdErr


  static class LambdaLogFacadeDelegate extends LambdaLogFacade {
    final Object logger;
    boolean once;

    LambdaLogFacadeDelegate (Class<?> clazzWhereLogger, Object actualLogger) {
      super(clazzWhereLogger);
      logger = actualLogger;
    }//new

    @Override public void warn (String msg, Throwable t) {
      try {
        staticWarnMethodHandle.invokeExact(logger, msg, t);// = logger.warn(msg, t)
      } catch (Throwable tex) {
        printToStdErr(loggerAppClassName, msg, t);
        if (!once) {
          printToStdErr(loggerAppClassName,
            "warn: "+staticWarnMethodHandle+".invokeExact "+logger+" failed! @ "+staticLoggerFactoryGetLoggerMethod,
            tex);
          once = true;
        }
      }
    }
  }//LambdaLogFacadeDelegate

  /** Method.setAccessible(true) makes invoke a little faster */
  private static Method setAccessible (Method method) {
    try { method.setAccessible(true); } catch (Throwable ignore) {}
    return method;
  }

  /** LoggerFactoryClassName → Class → get "LoggerFactory.getLogger" method (invoke → Logger) */
  static Method findLoggerFactoryGetLogger (String className) {
    try {
      ClassLoader clsLoader = Thread.currentThread().getContextClassLoader();
      Class<?> cls = clsLoader.loadClass(className);//e.g. class java.util.logging.Logger
      //System.out.println("[debug] findLoggerFactoryGetLogger: ContextClassLoader.loadClass="+cls);
      return setAccessible(cls.getMethod("getLogger", String.class));//more common than Class.class
    } catch (Throwable ignore) {}
    try {
      Class<?> cls = Class.forName(className);
      //System.out.println("[debug] findLoggerFactoryGetLogger: Class.forName="+cls);
      return setAccessible(cls.getMethod("getLogger", String.class));
    } catch (Throwable ignore) {}
    return null;
  }

  /** Reflection based analog of: "LoggerFactory.getLogger"(loggerName) */
  static Object invokeGetLogger (Method getLoggerFactoryMethod, Class<?> clazzWhereLogger) {
    try {
      return getLoggerFactoryMethod.invoke(null/*static*/, clazzWhereLogger.getName());
    } catch (Throwable t) {
      printToStdErr(LambdaLogFacade.class,
        "invokeGetLogger: LoggerFactory.getLogger("+clazzWhereLogger+") failed! @ "+getLoggerFactoryMethod, t);
      return null;
    }
  }

  static Method getWarnMethod (Object logger) {
    try {
      return setAccessible(logger.getClass().getMethod("warn", String.class/*msg*/, Throwable.class));
    } catch (Throwable t) {
      printToStdErr(LambdaLogFacade.class, "getWarnMethod: can't find method WARN in logger: "+logger, t);
      return null;
    }
  }
}