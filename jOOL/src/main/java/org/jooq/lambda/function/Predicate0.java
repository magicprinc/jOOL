/**
 * Copyright (c), Data Geekery GmbH, contact@datageekery.com
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

import org.jooq.lambda.tuple.Tuple0;

import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A Predicate with 0 arguments.
 *
 * @author Lukas Eder, Andrej Fink
 */
@FunctionalInterface
public interface Predicate0 extends Supplier<Boolean>, Function0<Boolean>, Callable<Boolean>, BooleanSupplier, GenericPredicate {

    @Override boolean getAsBoolean ();


    /**
     * Apply this Predicate to the arguments.
     */
    default boolean test() {
        return getAsBoolean();
    }

    /**
     * Apply this Predicate to the arguments.
     *
     * @param args The arguments as a tuple.
     */
    default boolean test(Tuple0 args) {
        return getAsBoolean();
    }

    /**
     * Predicate as {@link Supplier}
     */
    @Override
    default Boolean get() {
        return getAsBoolean();
    }

    /**
     * Predicate as {@link Callable}
     */
    @Override default Boolean call () {
        return getAsBoolean();
    }

    /**
     * Convert this Predicate to a {@link java.util.function.Supplier}
     */
    @Override default Supplier<Boolean> toSupplier() {
        return this;
    }

    /**
     * Convert to this Predicate from a {@link java.util.function.Supplier}
     */
    static Predicate0 from(Supplier<Boolean> supplier) {
        return supplier::get;
    }

    /**
     * Convert this Predicate to a {@link java.util.function.Supplier}
     */
    default BooleanSupplier toBooleanSupplier() {
        return this;
    }

    /**
     * Convert to this Predicate from a {@link java.util.function.Supplier}
     */
    static Predicate0 from(BooleanSupplier supplier) {
        return supplier::getAsBoolean;
    }

}