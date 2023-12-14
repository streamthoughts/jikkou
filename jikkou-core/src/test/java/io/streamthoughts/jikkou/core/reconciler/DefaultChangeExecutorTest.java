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
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultChangeExecutorTest {

    public static final ReconciliationContext CONTEXT_DRY_RUN_FALSE = ReconciliationContext.builder().dryRun(false).build();
    public static final ReconciliationContext CONTEXT_DRY_RUN_TRUE = ReconciliationContext.builder().dryRun(true).build();

    @Test
    void shouldNotExecuteHandlerForDryRunTrue() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(Operation.CREATE);
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec.builder()
                        .withOperation(Operation.CREATE)
                        .build()
                )
                .build();
        ChangeExecutor<ResourceChange> executor = new DefaultChangeExecutor<>(
                CONTEXT_DRY_RUN_TRUE,
                List.of(change)
        );

        // When
        List<ChangeResult> results = executor.applyChanges(List.of(handler));

        // Then
        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(handler.capturedChanges.isEmpty());
    }

    @Test
    void shouldExecuteHandlerForSupportedType() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(Operation.CREATE);
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec.builder()
                        .withOperation(Operation.CREATE)
                        .build()
                )
                .build();
        ChangeExecutor<ResourceChange> executor = new DefaultChangeExecutor<>(
                CONTEXT_DRY_RUN_FALSE,
                List.of(change)
        );

        // When
        List<ChangeResult> results = executor.applyChanges(List.of(handler));

        // Then
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(List.of(change), handler.capturedChanges);
    }

    @Test
    void shouldNotExecuteHandlerForUnsupportedType() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(Operation.DELETE);
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec.builder()
                        .withOperation(Operation.CREATE)
                        .build()
                )
                .build();
        ChangeExecutor<ResourceChange> executor = new DefaultChangeExecutor<>(
                CONTEXT_DRY_RUN_FALSE,
                List.of(change)
        );
        // When
        List<ChangeResult> results = executor.applyChanges(List.of(handler));

        // Then
        Assertions.assertTrue(results.isEmpty());
        Assertions.assertTrue(handler.capturedChanges.isEmpty());
    }

    @Test
    void shouldAlwaysReturnNoneChanges() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(Operation.NONE);
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec.builder()
                        .withOperation(Operation.NONE)
                        .build()
                )
                .build();
        ChangeExecutor<ResourceChange> executor = new DefaultChangeExecutor<>(
                CONTEXT_DRY_RUN_FALSE,
                List.of(change)
        );

        // When
        List<ChangeResult> results = executor.applyChanges(List.of(handler));

        // Then
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(List.of(change), handler.capturedChanges);
    }


    public static class TestChangeHandler extends BaseChangeHandler<ResourceChange> {

        List<ResourceChange> capturedChanges = new ArrayList<>();

        public TestChangeHandler(Operation type) {
            super(type);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            this.capturedChanges.addAll(changes);
            return changes.stream().map(ChangeResponse::new).toList();
        }

        @Override
        public TextDescription describe(@NotNull ResourceChange change) {
            return null;
        }
    }
}