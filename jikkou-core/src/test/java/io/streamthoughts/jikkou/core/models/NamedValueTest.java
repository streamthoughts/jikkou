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
package io.streamthoughts.jikkou.core.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NamedValueTest {

    @Test
    void shouldGetEmptySet() {
        NamedValue.Set values = NamedValue.emptySet();
        Assertions.assertTrue(values.isEmpty());
    }

    @Test
    void shouldGetSetOfMap() {
        Map<String, Object> map = Map.of("k1", "v1", "k2", "v2");
        NamedValue.Set values = NamedValue.setOf(map);
        Assertions.assertEquals(map, values.asMap());
    }

    @Test
    void shouldGetSetOfValues() {
        NamedValue.Set values = NamedValue.emptySet()
                .with(new NamedValue("k1", "v1"))
                .with(new NamedValue("k2", "v2"));

        Assertions.assertEquals(
                Map.of("k1", "v1", "k2", "v2"),
                values.asMap());
    }
}