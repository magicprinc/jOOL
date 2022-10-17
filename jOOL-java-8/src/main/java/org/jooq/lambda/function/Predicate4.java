/**
 * Copyright (c) Data Geekery GmbH, contact@datageekery.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jooq.lambda.function;


import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

/**
 * A Predicate with 4 arguments.
 *
 * @author Lukas Eder, Andrej Fink
 */
@FunctionalInterface
public interface Predicate4<T1,T2,T3,T4> extends GenericPredicate {

    /**
     * Apply this Predicate to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default boolean test(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return test(args.v1, args.v2, args.v3, args.v4);
    }

    /**
     * Apply this Predicate to the arguments.
     */
    boolean test(T1 v1, T2 v2, T3 v3, T4 v4);

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate3<T2, T3, T4> applyPartially(T1 v1) {
        return (v2, v3, v4) -> test(v1, v2, v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate2<T3, T4> applyPartially(T1 v1, T2 v2) {
        return (v3, v4) -> test(v1, v2, v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T4> applyPartially(T1 v1, T2 v2, T3 v3) {
        return (v4) -> test(v1, v2, v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(T1 v1, T2 v2, T3 v3, T4 v4) {
        return () -> test(v1, v2, v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate3<T2, T3, T4> applyPartially(Tuple1<? extends T1> args) {
        return (v2, v3, v4) -> test(args.v1, v2, v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate2<T3, T4> applyPartially(Tuple2<? extends T1, ? extends T2> args) {
        return (v3, v4) -> test(args.v1, args.v2, v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T4> applyPartially(Tuple3<? extends T1, ? extends T2, ? extends T3> args) {
        return (v4) -> test(args.v1, args.v2, args.v3, v4);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return () -> test(args.v1, args.v2, args.v3, args.v4);
    }

    /**
     * Convert this Predicate to a {@link Function4}.
     */
    default Function4<T1,T2,T3,T4,Boolean> toFunction() {
        return this::test;
    }

}