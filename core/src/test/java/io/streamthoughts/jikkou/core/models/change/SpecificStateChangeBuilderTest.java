/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpecificStateChangeBuilderTest {

    @Test
    void shouldBuildStateChangeForUpdateOp() {
        SpecificStateChange<String> change = new SpecificStateChangeBuilder<String>()
            .withBefore("foo")
            .withAfter("bar")
            .build();
        Assertions.assertEquals(Operation.UPDATE, change.getOp());
    }

    @Test
    void shouldBuildStateChangeForNoneOp() {
        SpecificStateChange<String> change = new SpecificStateChangeBuilder<String>()
            .withBefore("foo")
            .withAfter("foo")
            .build();
        Assertions.assertEquals(Operation.NONE, change.getOp());
    }

    @Test
    void shouldBuildStateChangeGivenComparatorReturningTrue() {
        SpecificStateChange<String> change = new SpecificStateChangeBuilder<String>()
            .withBefore("foo")
            .withAfter("bar")
            .withComparator((before, after) -> true)
            .build();
        Assertions.assertEquals(Operation.NONE, change.getOp());
    }

    @Test
    void shouldBuildStateChangeGivenComparatorReturningFalse() {
        SpecificStateChange<String> change = new SpecificStateChangeBuilder<String>()
            .withBefore("foo")
            .withAfter("bar")
            .withComparator((before, after) -> false)
            .build();
        Assertions.assertEquals(Operation.UPDATE, change.getOp());
    }

    @Test
    void shouldProperlySerializeChangeState() {
        SpecificStateChange<String> change = new SpecificStateChangeBuilder<String>()
                .withBefore("foo")
                .withAfter("bar")
                .build();

        ObjectMapper objectMapper = Jackson.JSON_OBJECT_MAPPER;

        assertDoesNotThrow(() -> {
            String changeAsString = objectMapper.writeValueAsString(change);
            assertNotNull(changeAsString);
        });
    }
}