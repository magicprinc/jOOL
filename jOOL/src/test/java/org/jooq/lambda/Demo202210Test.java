package org.jooq.lambda;

import org.jooq.lambda.fi.lang.SafeRunnable;
import org.jooq.lambda.function.GenericPredicate;
import org.jooq.lambda.function.Predicate3;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.Assert.*;

public class Demo202210Test {

  @Test
  public void wrapFunctionsAreFun () {
    final String tempDir = "/temp";

    // 1. create vol0.txt - vol2.txt files with text lines in temp
    Loops.forLoop(3, Unchecked.intConsumer(
      (i)->{
        Path file = Paths.get(tempDir, "vol" + i + ".txt");

        Stream<CharSequence> dataGen = Seq.range(1, 101).map(n -> "vol " + i + " line " + n);

        Files.write(file, dataGen::iterator, UTF_8, CREATE);

      }, Wrap.LOG_WARN // Exceptions - if any - go to log
    ));

    // 2. create empty file (Exceptions - if any - go to log)
    Wrap.run(()-> Files.createFile(Paths.get(tempDir, "empty.text")));

    List<String> linesFromFiles = Seq.of("vol0", "vol1", "vol2", "empty", "none", "////:::")
      .map((s)->s+".txt")
      .map(Wrap.function(f->Paths.get(tempDir, f)))// InvalidPathException
      .filter(Wrap.predicate(p->p.isPresent() && Files.isRegularFile(p.v1) && Files.size(p.v1)>10))// Exception→log, returns false
      .map(Either::v1)
      .map(Wrap.function(p->Files.lines(p, UTF_8)))// seq of Either with lines
      .filter(Either::isPresent)
      // .flatMap(Either::stream)
      .flatMap(Either::v1)
      .toList();

    assertEquals(3*100, linesFromFiles.size());
    assertEquals("vol 0 line 1", linesFromFiles.get(0));
    assertEquals("vol 2 line 100", linesFromFiles.get(linesFromFiles.size()-1));

    int size = 1492-200+(System.lineSeparator().length())*100;
    Either<Long> sz = Wrap.call(() -> Files.size(Paths.get(tempDir, "vol0.txt")));
    assertTrue(sz.isSuccess());
    assertEquals(size, sz.v1.intValue());
  }


  @Test
  public void safeRunnable () {
    ExecutorService executor = Executors.newCachedThreadPool();

    Thread.setDefaultUncaughtExceptionHandler(Wrap.LOG_WARN_UNCAUGHT_EXCEPTION_HANDLER);

    CountDownLatch startSignal = new CountDownLatch(1);

    SafeRunnable r = new SafeRunnable() { // can be implemented
      @Override public void execute () throws Throwable {
        Thread.currentThread().setUncaughtExceptionHandler(Wrap.LOG_WARN_UNCAUGHT_EXCEPTION_HANDLER);

        startSignal.await();
        throw new IOException("Try to kill Executor");
      }
    };

    Loops.loop(10, ()->executor.execute(r));

    startSignal.countDown();

    Wrap.run(()->Thread.sleep(1000));

    executor.shutdownNow();
  }


  @Test public void testPredicate () {
    Predicate3<Integer,Integer,Integer> condition1 = (a,b,c)->c==b*b && b == a*a;

    Loops.Incrementer looper = new Loops.Incrementer(3, 100);

    long sz = looper.seq()
      .map(Loops.Incrementer::toIntegerArray)
      .filter(array -> GenericPredicate.testDynamicPredicateVarArgs(condition1, (Object[])array))
      .peek(array -> System.out.println(Arrays.toString(array)))
      .count();
    assertEquals(4, sz);

    sz = looper.seq()
      .map(Loops.Incrementer::toIntegerArray)
      .map(Wrap::tuple)
      .filter(t -> condition1.test(Wrap.castUnsafe(t)))
      .peek(System.out::println)
      .count();
    assertEquals(4, sz);
  }

  int cntDir;
  int cntFiles;
  int cntFilesWithCr;
  long totalSize;

  Object THIS = this;

  @Test
  public void realTaskCrLfFiles () throws Throwable {
    Path start = Paths.get("src");
    Loops.recur(start, (file, fun)->{
      if (Files.isRegularFile(file)) {
        cntFiles++;

        byte[] bytes = Files.readAllBytes(file);
        totalSize += bytes.length;

        for (byte b : bytes) {
          if (b == '\r') {
            cntFilesWithCr++;
            System.out.println(file);
            break;
          }
        }//f
      } else {
        cntDir++;

        try (Stream<Path> list = Files.list(file)){
          list.forEach(fun);
        }
      }//i file -or- dir
    });
    System.out.println(start.toAbsolutePath()+"\n\tdirs: "+cntDir+"\tTotal files: "+cntFiles
      +", with CR: "+cntFilesWithCr+", bytes: "+totalSize);
  }


  @Test
  public void recur () throws Throwable {
    assertThrows(IOException.class, ()-> Loops.recur(null, (obj,thiz)->{
      System.out.println("this: "+this+" = "+this.getClass());
      System.out.println(Arrays.toString(this.getClass().getDeclaredClasses()));
      System.out.println(Arrays.toString(this.getClass().getClasses()));

      System.out.println("thiZ: "+thiz+" = "+thiz.getClass());
      System.out.println(Arrays.toString(thiz.getClass().getDeclaredClasses()));
      System.out.println(Arrays.toString(thiz.getClass().getClasses()));
      assertTrue(thiz instanceof Loops.SafeSelfConsumer);
      assertSame(THIS, this);
      assertNull(obj);
      throw new IOException("fake");
    }));

    Long r = Loops.calc(5L, (n,fun)-> n>1?n*fun.apply(n-1):1);
    assertEquals(5*4*3*2, r.intValue());
  }
}