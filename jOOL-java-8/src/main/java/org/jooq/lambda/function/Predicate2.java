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

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * A Predicate with 2 arguments.
 *
 * @author Lukas Eder, Andrej Fink
 */
@FunctionalInterface
public interface Predicate2<T1,T2> extends BiPredicate<T1,T2>, GenericPredicate {

    /**
     * Apply this Predicate to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default boolean test(Tuple2<? extends T1, ? extends T2> args) {
        return test(args.v1, args.v2);
    }

    /**
     * Apply this Predicate to the arguments.
     */
    @Override
    boolean test(T1 v1, T2 v2);

    /**
     * Convert this Predicate to a {@link java.util.function.BiPredicate}.
     */
    default BiPredicate<T1,T2> toBiPredicate() {
        return this;
    }

    /**
     * Convert to this Predicate to a {@link java.util.function.BiPredicate}.
     */
    static <T1, T2> Predicate2<T1, T2> from(BiPredicate<? super T1, ? super T2> jufBiPredicate) {
        return jufBiPredicate::test;
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T2> applyPartially(T1 v1) {
        return (v2) -> test(v1, v2);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(T1 v1, T2 v2) {
        return () -> test(v1, v2);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate1<T2> applyPartially(Tuple1<? extends T1> args) {
        return (v2) -> test(args.v1, v2);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(Tuple2<? extends T1, ? extends T2> args) {
        return () -> test(args.v1, args.v2);
    }

    /**
     * Convert this Predicate to a {@link Function2}.
     */
    default Function2<T1, T2, Boolean> toFunction() {
        return this::test;
    }

    /**
     * Convert to this Predicate to a {@link java.util.function.BiFunction}.
     */
    static <T1, T2> Predicate2<T1,T2> fromFunction (BiFunction<? super T1, ? super T2, ? extends Boolean> function) {
        return function::apply;
    }

}