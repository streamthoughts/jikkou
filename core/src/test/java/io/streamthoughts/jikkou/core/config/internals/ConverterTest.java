/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.config.internals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConverterTest {

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
