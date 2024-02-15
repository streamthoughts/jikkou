/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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