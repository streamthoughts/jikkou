/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiOptionSpecTest {

    @Test
    void shouldCreateApiOptionSpecForString() {
        // Given
        ApiOptionSpec spec = new ApiOptionSpec(
                "string",
                "string option",
                String.class,
                "default",
                false
        );
        // Then
        Assertions.assertEquals(String.class.getSimpleName(), spec.type());
        Assertions.assertEquals(String.class, spec.typeClass());
        Assertions.assertNull(spec.enumSpec());
    }

    @Test
    void shouldCreateApiOptionSpecForEnum() {
        // Given
        ApiOptionSpec spec = new ApiOptionSpec(
                "enum",
                "enum option",
                TestEnum.class,
                null,
                false
        );
        // Then
        Assertions.assertEquals(String.class.getSimpleName(), spec.type()); // FALLBACK TO STRING
        Assertions.assertEquals(String.class, spec.typeClass()); // FALLBACK TO STRING

        ApiOptionSpec.EnumSpec expected = new ApiOptionSpec.EnumSpec(
                TestEnum.class.getSimpleName(),
                Arrays.stream(TestEnum.values()).map(Enum::name).collect(Collectors.toSet()));
        Assertions.assertEquals(expected, spec.enumSpec());
    }

    private enum TestEnum {
        FOO, BAR
    }
}