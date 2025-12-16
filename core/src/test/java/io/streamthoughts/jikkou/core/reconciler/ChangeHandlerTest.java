/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeHandlerTest {

    @Test
    void shouldGetDefaultChangeHandler() {
        // Given
        ChangeHandler.None handler = new ChangeHandler.None(change -> () -> "NONE");
        ResourceChange change = GenericResourceChange.builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .build()
                )
                .build();

        // When / Then
        Set<Operation> supportedOperations = handler.supportedChangeTypes();
        Assertions.assertEquals(1, supportedOperations.size());
        Assertions.assertEquals(Operation.NONE, handler.supportedChangeTypes().iterator().next());

        // When
        List<ChangeResponse> results = handler.handleChanges(List.of(change));

        // Then
        List<ChangeResponse> expected = List.of(new ChangeResponse(change));
        Assertions.assertEquals(expected, results);

        // When / Then
        TextDescription description = handler.describe(change);
        Assertions.assertNotNull(description);
        Assertions.assertEquals("NONE", description.textual());
    }
}