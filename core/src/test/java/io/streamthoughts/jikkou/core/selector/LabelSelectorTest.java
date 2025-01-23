/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LabelSelectorTest {

    static final TestResource TEST_RESOURCE = new TestResource()
        .withMetadata(ObjectMeta
            .builder()
            .withLabel("env", "prod")
            .build()
        );

    @Test
    public void shouldReturnTrueGivenExistingLabel() {
        // GIVEN
        var expression = new PreparedExpression(
            "env",
            ExpressionOperator.EXISTS, List.of()
        );
        LabelSelector selector = new LabelSelector(expression);

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseGivenNonExistingLabel() {
        // GIVEN
        var expression = new PreparedExpression(
            "any",
            ExpressionOperator.EXISTS, List.of()
        );
        LabelSelector selector = new LabelSelector(expression);

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueGivenMatchingLabel() {
        // GIVEN
        var expression = new PreparedExpression(
            "env",
            ExpressionOperator.IN, List.of("prod", "staging")
        );
        LabelSelector selector = new LabelSelector(expression);

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseGivenNonMatchingLabel() {
        // GIVEN
        var expression = new PreparedExpression(
            "env",
            ExpressionOperator.NOTIN, List.of("prod")
        );
        LabelSelector selector = new LabelSelector(expression);

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertFalse(result);
    }
}