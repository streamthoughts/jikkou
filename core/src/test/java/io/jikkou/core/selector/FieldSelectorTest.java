/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.selector;

import io.jikkou.core.TestResource;
import io.jikkou.core.models.ObjectMeta;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FieldSelectorTest {

    static final TestResource TEST_RESOURCE = new TestResource()
            .withMetadata(ObjectMeta
                    .builder()
                    .withName("test-resource")
                    .withLabel("a-label.key", "value")
                    .withAnnotation("an-annotation.key", "value")
                    .build()
            );


    @Test
    public void shouldSelectResourceForSelectorIn() {
        // GIVEN
        var expression =  new PreparedExpression(
                "",
                "metadata.name",
                ExpressionOperator.IN, (List.of("test-resource"))
        );
        FieldSelector selector = new FieldSelector(expression);

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertTrue(result);
    }

    @Test
    public void shouldNotSelectResourceForSelectorNotIn() {
        // GIVEN
        var expression =  new PreparedExpression(
                "",
                "metadata.name",
                ExpressionOperator.NOTIN, (List.of("test-resource"))
        );
        FieldSelector selector = new FieldSelector(expression);

        // WHEN
        boolean result = selector.apply(TEST_RESOURCE);

        // THEN
        Assertions.assertFalse(result);
    }
}