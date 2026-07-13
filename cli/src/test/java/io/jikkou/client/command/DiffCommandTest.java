/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.reconciler.Operation;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DiffCommandTest {

    @Test
    void shouldDetectChanges_whenAnyOperationIsNotNone() {
        List<ResourceChange> changes = List.of(
                changeWithOp(Operation.NONE),
                changeWithOp(Operation.UPDATE)
        );
        Assertions.assertTrue(DiffCommand.hasChanges(changes));
    }

    @Test
    void shouldDetectNoChanges_whenAllOperationsAreNone() {
        List<ResourceChange> changes = List.of(
                changeWithOp(Operation.NONE),
                changeWithOp(Operation.NONE)
        );
        Assertions.assertFalse(DiffCommand.hasChanges(changes));
    }

    @Test
    void shouldCountDeleteAsChange() {
        Assertions.assertTrue(DiffCommand.hasChanges(List.of(changeWithOp(Operation.DELETE))));
    }

    @Test
    void shouldCountReplaceAsChange() {
        Assertions.assertTrue(DiffCommand.hasChanges(List.of(changeWithOp(Operation.REPLACE))));
    }

    @Test
    void shouldSummarizeChangesByOperation() {
        List<ResourceChange> changes = List.of(
                changeWithOp(Operation.CREATE),
                changeWithOp(Operation.UPDATE),
                changeWithOp(Operation.UPDATE),
                changeWithOp(Operation.NONE)
        );
        Assertions.assertEquals("3 changes detected: 1 CREATE, 2 UPDATE", DiffCommand.summarize(changes));
    }

    @Test
    void shouldSummarizeSingleChange() {
        List<ResourceChange> changes = List.of(changeWithOp(Operation.DELETE));
        Assertions.assertEquals("1 change detected: 1 DELETE", DiffCommand.summarize(changes));
    }

    @Test
    void shouldSummarizeNoChanges() {
        List<ResourceChange> changes = List.of(changeWithOp(Operation.NONE));
        Assertions.assertEquals("No changes detected.", DiffCommand.summarize(changes));
    }

    private static ResourceChange changeWithOp(Operation op) {
        return GenericResourceChange.builder()
                .withSpec(ResourceChangeSpec.builder()
                        .withOperation(op)
                        .build()
                )
                .build();
    }
}
