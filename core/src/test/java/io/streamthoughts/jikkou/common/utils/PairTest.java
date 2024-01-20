/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.common.utils;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PairTest {

    @Test
    void shouldGetPairFormMapEntry() {
        Pair<Object, Object> pair = Pair.of(new Map.Entry<>() {
            @Override
            public Object getKey() {
                return "key";
            }

            @Override
            public Object getValue() {
                return "value";
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        });
        Assertions.assertEquals(new Pair<>("key", "value"), pair);
    }

    @Test
    void shouldMapPairRight() {
        Assertions.assertEquals(Pair.of("key", "VALUE"), Pair.of("key", "value").mapRight(String::toUpperCase));
    }

    @Test
    void shouldMapPairLeft() {
        Assertions.assertEquals(Pair.of("KEY", "value"), Pair.of("key", "value").mapLeft(String::toUpperCase));
    }
}