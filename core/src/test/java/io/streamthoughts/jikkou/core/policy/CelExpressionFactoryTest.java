/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CelExpressionFactoryTest {

    @Test
    void shouldFailedGivenExpressionNotReturningBoolean() {
        var expression = "'text'";

        // When
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CelExpressionFactory.bool().compile(expression);
        });

        Assertions.assertEquals(
            "Failed to type-check expression: 'text'. Reason: ERROR: <input>:1:1: expected type 'bool' but found 'string'\n" +
                " | 'text'\n" +
                " | ^",
            exception.getMessage()
        );
    }


    @ParameterizedTest
    @ValueSource(strings = {
        "resource.kind == 'Test'",
        "resource.metadata.name.startsWith('Test')",
        "resource.spec.number >= 10",
        "resource.spec.bool",
        "type(resource.spec.bool) in [bool]",
        "size(resource.spec.array) == 3",
        "has(resource.metadata.name)",
    })
    void shouldCompileExpressionEvaluatingToTrue(String expression) {
        // Given
        var resource = new GenericResource(
            "io.jikkou/v1",
            "Test",
            new ObjectMeta("TestName"),
            null,
            Map.of(
                "spec", Map.of(
                    "number", 42,
                    "bool", true,
                    "array", List.of("one", "two", "three")
                )
            )
        );

        // When
        CelExpression<Boolean> compiled = CelExpressionFactory.bool().compile(expression);

        // Then
        Assertions.assertNotNull(compiled);
        Assertions.assertTrue(compiled.eval(resource));
    }

    @Test
    void shouldCompileExpressionEvaluatingToFalse() {
        // Given
        var resource = new GenericResource(
            "io.jikkou/v1",
            "Test",
            new ObjectMeta("???"),
            null,
            null

        );

        var expression = "resource.metadata.name == 'test'";

        // When
        CelExpression<Boolean> compiled = CelExpressionFactory.bool().compile(expression);

        // Then
        Assertions.assertNotNull(compiled);
        Assertions.assertFalse(compiled.eval(resource));
    }

    @Test
    void shouldSucceedCompileExpressionEvaluatingToStringWhenStringIsExpected() {
        // Given
        var resource = new GenericResource(
            "io.jikkou/v1",
            "Test",
            new ObjectMeta("name"),
            null,
            null

        );

        var expression = "'Resource name is: ' + resource.metadata.name";

        // When
        CelExpression<String> compiled = CelExpressionFactory.string().compile(expression);

        // Then
        Assertions.assertNotNull(compiled);
        Assertions.assertEquals("Resource name is: name", compiled.eval(resource));
    }

    @Test
    void shouldFailedCompileExpressionEvaluatingToStringWhenStringIsExpected() {
        // Given
        var resource = new GenericResource(
            "io.jikkou/v1",
            "Test",
            new ObjectMeta("name"),
            null,
            null

        );

        var expression = "true";

        // When
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            CelExpressionFactory.string().compile(expression);
        });

        Assertions.assertEquals(
            "Failed to type-check expression: true. Reason: ERROR: <input>:1:1: expected type 'string' but found 'bool'\n" +
                " | true\n" +
                " | ^",
            exception.getMessage()
        );
    }
}