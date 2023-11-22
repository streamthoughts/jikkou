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
package io.streamthoughts.jikkou.core.config.internals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TypeTest {

    @Test
    void shouldGetTypeFromString() {
        Assertions.assertEquals(Type.STRING, Type.forClass(String.class));
    }

    @Test
    void shouldGetTypeFromDouble() {
        Assertions.assertEquals(Type.DOUBLE, Type.forClass(Double.class));
    }

    @Test
    void shouldGetTypeFromLong() {
        Assertions.assertEquals(Type.LONG, Type.forClass(Long.class));
    }

    @Test
    void shouldGetTypeFromInteger() {
        Assertions.assertEquals(Type.INTEGER, Type.forClass(Integer.class));
    }

    @Test
    void shouldGetTypeFromShort() {
        Assertions.assertEquals(Type.SHORT, Type.forClass(Short.class));
    }

    @Test
    void shouldGetTypeFromBoolean() {
        Assertions.assertEquals(Type.BOOLEAN, Type.forClass(Boolean.class));
    }

    @Test
    void shouldGetTypeFromList() {
        Assertions.assertEquals(Type.LIST, Type.forClass(List.class));
    }

    @Test
    void shouldGetTypeFromSet() {
        Assertions.assertEquals(Type.LIST, Type.forClass(Set.class));
    }

    @Test
    void shouldGetTypeFromFloat() {
        Assertions.assertEquals(Type.FLOAT, Type.forClass(Float.class));
    }
}
