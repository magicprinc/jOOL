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

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A Predicate with 1 arguments.
 *
 * @author Lukas Eder, Andrej Fink
 */
@FunctionalInterface
public interface Predicate1<T1> extends Predicate<T1>, GenericPredicate {

    /**
     * Apply this Predicate to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default boolean test(Tuple1<? extends T1> args) {
        return test(args.v1);
    }

    /**
     * Apply this Predicate to the arguments.
     */
    @Override
    boolean test(T1 v1);

    /**
     * Convert this Predicate to a {@link java.util.function.Predicate}.
     */
    default Predicate<T1> toPredicate() {
        return this;
    }

    /**
     * Convert to this Predicate from a {@link java.util.function.Predicate}.
     */
    static <T1> Predicate1<T1> from(Predicate<? super T1> jufPredicate) {
        return jufPredicate::test;
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(T1 v1) {
        return () -> test(v1);
    }

    /**
     * Partially apply this Predicate to the arguments.
     */
    default Predicate0 applyPartially(Tuple1<? extends T1> args) {
        return () -> test(args.v1);
    }

    /**
     * Convert this Predicate to a {@link Function1}.
     */
    default Function1<T1, Boolean> toFunction() {
        return this::test;
    }

    /**
     * Convert to this Predicate from a {@link java.util.function.Function}.
     */
    static <T1> Predicate1<T1> fromFunction (Function<? super T1, Boolean> function) {
        return function::apply;
    }
}