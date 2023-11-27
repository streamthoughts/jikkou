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