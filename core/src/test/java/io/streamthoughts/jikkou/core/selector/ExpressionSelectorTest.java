/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.selector;

import io.streamthoughts.jikkou.core.TestResource;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpressionSelectorTest {

    static final TestResource TEST_RESOURCE = new TestResource()
        .withMetadata(ObjectMeta
            .builder()
            .withLabel("env", "prod")
            .build()
        );

    @Test
    public void shouldReturnTrueGivenExistingLabel() {
        // GIVEN
        ExpressionSelector selector = new ExpressionSelector("has(resource.metadata.labels.env)");

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseGivenNonExistingLabel() {
        // GIVEN
        ExpressionSelector selector = new ExpressionSelector("has(resource.metadata.labels.any)");

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertFalse(result);
    }

    @Test
    public void shouldReturnTrueGivenMatchingLabel() {
        // GIVEN
        ExpressionSelector selector = new ExpressionSelector("resource.metadata.labels.env in ['prod', 'staging']");

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertTrue(result);
    }

    @Test
    public void shouldReturnFalseGivenNonMatchingLabel() {
        // GIVEN
        ExpressionSelector selector = new ExpressionSelector("resource.metadata.labels.env in ['dev']");

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertFalse(result);
    }
}