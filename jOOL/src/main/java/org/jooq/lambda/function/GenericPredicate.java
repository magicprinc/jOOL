package org.jooq.lambda.function;

import java.util.Arrays;

/**
 Common ancestor for all our Predicates 0..16.
 And dangerous dynamic (no help from compiler! All errors in runtime only!) invocations.
 <br><br>
 !!! For very special cases ONLY!!!

 @author Andrej Fink
 */
public interface GenericPredicate {

  /**
   args.length is used to cast generic variable predicateWithArgsLenArgs to real PredicateX.

   E.g. if args.length == 1 → Predicate1 will be called.
   If predicateWithArgsLenArgs contains another type (e.g. Predicate3) → ClassCastException will be thrown.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static boolean testDynamicArgCntToPredicate (GenericPredicate predicateWithArgsLenArgs, Object... args) throws ClassCastException, IllegalArgumentException {
    if (args == null || args.length == 0) {
      return ((Predicate0) predicateWithArgsLenArgs).test();
    }
    switch (args.length) {
    case  1: return ((Predicate1) predicateWithArgsLenArgs).test(args[0]);
    case  2: return ((Predicate2) predicateWithArgsLenArgs).test(args[0],args[1]);
    case  3: return ((Predicate3) predicateWithArgsLenArgs).test(args[0],args[1],args[2]);
    case  4: return ((Predicate4) predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3]);
    case  5: return ((Predicate5) predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4]);
    case  6: return ((Predicate6) predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5]);
    case  7: return ((Predicate7) predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
    case  8: return ((Predicate8) predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7]);
    case  9: return ((Predicate9) predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8]);
    case 10: return ((Predicate10)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9]);
    case 11: return ((Predicate11)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10]);
    case 12: return ((Predicate12)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11]);
    case 13: return ((Predicate13)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12]);
    case 14: return ((Predicate14)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13]);
    case 15: return ((Predicate15)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13],args[14]);
    case 16: return ((Predicate16)predicateWithArgsLenArgs).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13],args[14],args[15]);
    default: throw new IllegalArgumentException("Args.length must be in range [0..16], but "+args.length+": "+ Arrays.toString(args));
    }
  }

  /**
   A runtime object-type referenced from masterPredicateToSatisfy is used (e.g. MyPredicate1) to call it
   with required quantity of args.
   If optArgs.length is less than required - missing arguments will be filled with null:
   <pre><code>
   masterPredicateToSatisfy = new MyPredicate2();
   → `Predicate2.test(a)` → Predicate2.test(a, null)
   </code></pre>
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static boolean testDynamicPredicateVarArgs (GenericPredicate masterPredicateToSatisfy, Object... optArgs) throws ClassCastException, IllegalArgumentException, NullPointerException {
    final Object[] args = new Object[16];// 0..15 >= optArgs.length; extra args are null
    if (optArgs != null && optArgs.length>0) {
      System.arraycopy(optArgs, 0, args, 0, Math.min(16, optArgs.length));
    }

    if (masterPredicateToSatisfy instanceof Predicate0) {
      return ((Predicate0) masterPredicateToSatisfy).test();
    } else if (masterPredicateToSatisfy instanceof Predicate1) {
      return ((Predicate1) masterPredicateToSatisfy).test(args[0]);
    } else if (masterPredicateToSatisfy instanceof Predicate2) {
      return ((Predicate2) masterPredicateToSatisfy).test(args[0],args[1]);
    } else if (masterPredicateToSatisfy instanceof Predicate3) {
      return ((Predicate3) masterPredicateToSatisfy).test(args[0],args[1],args[2]);
    } else if (masterPredicateToSatisfy instanceof Predicate4) {
      return ((Predicate4) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3]);
    } else if (masterPredicateToSatisfy instanceof Predicate5) {
      return ((Predicate5) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4]);
    } else if (masterPredicateToSatisfy instanceof Predicate6) {
      return ((Predicate6) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5]);
    } else if (masterPredicateToSatisfy instanceof Predicate7) {
      return ((Predicate7) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6]);
    } else if (masterPredicateToSatisfy instanceof Predicate8) {
      return ((Predicate8) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7]);
    } else if (masterPredicateToSatisfy instanceof Predicate9) {
      return ((Predicate9) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8]);
    } else if (masterPredicateToSatisfy instanceof Predicate10) {
      return ((Predicate10) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9]);
    } else if (masterPredicateToSatisfy instanceof Predicate11) {
      return ((Predicate11) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10]);
    } else if (masterPredicateToSatisfy instanceof Predicate12) {
      return ((Predicate12) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11]);
    } else if (masterPredicateToSatisfy instanceof Predicate13) {
      return ((Predicate13) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12]);
    } else if (masterPredicateToSatisfy instanceof Predicate14) {
      return ((Predicate14) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13]);
    } else if (masterPredicateToSatisfy instanceof Predicate15) {
      return ((Predicate15) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13],args[14]);
    } else if (masterPredicateToSatisfy instanceof Predicate16) {
      return ((Predicate16) masterPredicateToSatisfy).test(args[0],args[1],args[2],args[3],args[4],args[5],args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13],args[14],args[15]);
    }
    throw new IllegalArgumentException("Unknown GenericPredicate successor: "+masterPredicateToSatisfy+", args: "+Arrays.toString(args));
  }

}