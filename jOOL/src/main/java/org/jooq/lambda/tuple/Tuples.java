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
package org.jooq.lambda.tuple;

import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;

import java.util.Optional;

/**
 * @author Lukas Eder
 */
final class Tuples {

    @SuppressWarnings("unchecked")
    static <T> int compare(T t1, T t2) {
        return t1 == null && t2 == null
             ? 0
             : t1 == null
             ? 1
             : t2 == null
             ? -1
             : ((Comparable<T>) t1).compareTo(t2);
    }
}
