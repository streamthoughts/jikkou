/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NamedValueTest {

    @Test
    void shouldGetEmptySet() {
        NamedValueSet values = NamedValueSet.emptySet();
        Assertions.assertTrue(values.isEmpty());
    }

    @Test
    void shouldGetSetOfMap() {
        Map<String, Object> map = Map.of("k1", "v1", "k2", "v2");
        NamedValueSet values = NamedValueSet.setOf(map);
        Assertions.assertEquals(map, values.asMap());
    }

    @Test
    void shouldGetSetOfValues() {
        NamedValueSet values = NamedValueSet.emptySet()
                .with(new NamedValue("k1", "v1"))
                .with(new NamedValue("k2", "v2"));

        Assertions.assertEquals(
                Map.of("k1", "v1", "k2", "v2"),
                values.asMap());
    }
}