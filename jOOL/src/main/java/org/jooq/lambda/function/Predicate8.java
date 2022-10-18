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
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;

/**
 * A Predicate with 8 arguments.
 *
 * @author Lukas Eder, Andrej Fink
 */
@FunctionalInterface
public interface Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> extends GenericPredicate {

    /**
     * Apply this Predicate to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default boolean test(Tuple8<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8> args) {
        return test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8);
    }

    /**
     * Apply this Predicate to the arguments.
     */
    boolean test(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8);

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate7<T2, T3, T4, T5, T6, T7, T8> applyPartially(T1 v1) {
        return (v2, v3, v4, v5, v6, v7, v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate6<T3, T4, T5, T6, T7, T8> applyPartially(T1 v1, T2 v2) {
        return (v3, v4, v5, v6, v7, v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate5<T4, T5, T6, T7, T8> applyPartially(T1 v1, T2 v2, T3 v3) {
        return (v4, v5, v6, v7, v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate4<T5, T6, T7, T8> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4) {
        return (v5, v6, v7, v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate3<T6, T7, T8> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return (v6, v7, v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate2<T7, T8> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return (v7, v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T8> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
        return (v8) -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8) {
        return () -> test(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate7<T2, T3, T4, T5, T6, T7, T8> applyPartially(Tuple1<? extends T1> args) {
        return (v2, v3, v4, v5, v6, v7, v8) -> test(args.v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate6<T3, T4, T5, T6, T7, T8> applyPartially(Tuple2<? extends T1, ? extends T2> args) {
        return (v3, v4, v5, v6, v7, v8) -> test(args.v1, args.v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate5<T4, T5, T6, T7, T8> applyPartially(Tuple3<? extends T1, ? extends T2, ? extends T3> args) {
        return (v4, v5, v6, v7, v8) -> test(args.v1, args.v2, args.v3, v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate4<T5, T6, T7, T8> applyPartially(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return (v5, v6, v7, v8) -> test(args.v1, args.v2, args.v3, args.v4, v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate3<T6, T7, T8> applyPartially(Tuple5<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5> args) {
        return (v6, v7, v8) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate2<T7, T8> applyPartially(Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6> args) {
        return (v7, v8) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T8> applyPartially(Tuple7<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7> args) {
        return (v8) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, v8);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(Tuple8<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8> args) {
        return () -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8);
    }

    /**
     * Convert this Predicate to a {@link Function8}.
     */
    default Function8<T1,T2,T3,T4,T5,T6,T7,T8,Boolean> toFunction() {
        return this::test;
    }


}