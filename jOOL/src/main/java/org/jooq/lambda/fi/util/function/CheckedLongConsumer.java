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
package org.jooq.lambda.fi.util.function;

import org.jooq.lambda.Sneaky;
import org.jooq.lambda.Unchecked;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * A {@link LongConsumer} that allows for checked exceptions.
 *
 * @author Lukas Eder
 */
@FunctionalInterface
public interface CheckedLongConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param value the input argument
     */
    void accept(long value) throws Throwable;

    /**
     * {@link Sneaky#longConsumer(CheckedLongConsumer)}
     */
    static LongConsumer sneaky(CheckedLongConsumer consumer) {
        return Sneaky.longConsumer(consumer);
    }

    /**
     * {@link Unchecked#longConsumer(CheckedLongConsumer)}
     */
    static LongConsumer unchecked(CheckedLongConsumer consumer) {
        return Unchecked.longConsumer(consumer);
    }

    /**
     * {@link Unchecked#longConsumer(CheckedLongConsumer, Consumer)}
     */
    static LongConsumer unchecked(CheckedLongConsumer consumer, Consumer<Throwable> handler) {
        return Unchecked.longConsumer(consumer, handler);
    }
}