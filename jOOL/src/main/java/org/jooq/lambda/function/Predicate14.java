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
import org.jooq.lambda.tuple.Tuple10;
import org.jooq.lambda.tuple.Tuple11;
import org.jooq.lambda.tuple.Tuple12;
import org.jooq.lambda.tuple.Tuple13;
import org.jooq.lambda.tuple.Tuple14;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import org.jooq.lambda.tuple.Tuple7;
import org.jooq.lambda.tuple.Tuple8;
import org.jooq.lambda.tuple.Tuple9;

/**
 * A Predicate with 14 arguments.
 *
 * @author Lukas Eder, Andrej Fink
 */
@FunctionalInterface
public interface Predicate14<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14> extends GenericPredicate {

    /**
     * Apply this Predicate to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default boolean test(Tuple14<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13, ? extends T14> args) {
        return test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, args.v10, args.v11, args.v12, args.v13, args.v14);
    }

    /**
     * Apply this Predicate to the arguments.
     */
    boolean test(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14);

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1) {
        return (v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate12<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2) {
        return (v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate11<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3) {
        return (v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate10<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4) {
        return (v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate9<T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5) {
        return (v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate8<T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6) {
        return (v7, v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate7<T8, T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7) {
        return (v8, v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate6<T9, T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8) {
        return (v9, v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate5<T10, T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9) {
        return (v10, v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate4<T11, T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10) {
        return (v11, v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate3<T12, T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11) {
        return (v12, v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate2<T13, T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12) {
        return (v13, v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T14> applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13) {
        return (v14) -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(T1 v1, T2 v2, T3 v3, T4 v4, T5 v5, T6 v6, T7 v7, T8 v8, T9 v9, T10 v10, T11 v11, T12 v12, T13 v13, T14 v14) {
        return () -> test(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate13<T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple1<? extends T1> args) {
        return (v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate12<T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple2<? extends T1, ? extends T2> args) {
        return (v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate11<T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple3<? extends T1, ? extends T2, ? extends T3> args) {
        return (v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate10<T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple4<? extends T1, ? extends T2, ? extends T3, ? extends T4> args) {
        return (v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate9<T6, T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple5<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5> args) {
        return (v6, v7, v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate8<T7, T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple6<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6> args) {
        return (v7, v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate7<T8, T9, T10, T11, T12, T13, T14> applyPartially(Tuple7<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7> args) {
        return (v8, v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate6<T9, T10, T11, T12, T13, T14> applyPartially(Tuple8<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8> args) {
        return (v9, v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate5<T10, T11, T12, T13, T14> applyPartially(Tuple9<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9> args) {
        return (v10, v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate4<T11, T12, T13, T14> applyPartially(Tuple10<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10> args) {
        return (v11, v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, args.v10, v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate3<T12, T13, T14> applyPartially(Tuple11<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11> args) {
        return (v12, v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, args.v10, args.v11, v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate2<T13, T14> applyPartially(Tuple12<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12> args) {
        return (v13, v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, args.v10, args.v11, args.v12, v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T14> applyPartially(Tuple13<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13> args) {
        return (v14) -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, args.v10, args.v11, args.v12, args.v13, v14);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(Tuple14<? extends T1, ? extends T2, ? extends T3, ? extends T4, ? extends T5, ? extends T6, ? extends T7, ? extends T8, ? extends T9, ? extends T10, ? extends T11, ? extends T12, ? extends T13, ? extends T14> args) {
        return () -> test(args.v1, args.v2, args.v3, args.v4, args.v5, args.v6, args.v7, args.v8, args.v9, args.v10, args.v11, args.v12, args.v13, args.v14);
    }

    /**
     * Convert this Predicate to a {@link Function14}.
     */
    default Function14<T1,T2,T3,T4,T5,T6,T7,T8,T9,T10,T11,T12,T13,T14,Boolean> toFunction() {
        return this::test;
    }

}