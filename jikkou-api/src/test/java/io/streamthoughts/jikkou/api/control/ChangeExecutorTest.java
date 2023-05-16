/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.api.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChangeExecutorTest {

    @Test
    void shouldNotExecuteHandlerForDryRunTrue() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(ChangeType.ADD);
        Change change = () -> ChangeType.ADD;
        ChangeExecutor<Change> executor = new ChangeExecutor<>(List.of(handler));

        // When
        List<ChangeResult<Change>> results = executor.execute(List.of(change), true);

        // Then
        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(handler.capturedChanges.isEmpty());
    }

    @Test
    void shouldExecuteHandlerForSupportedType() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(ChangeType.ADD);
        Change change = () -> ChangeType.ADD;
        ChangeExecutor<Change> executor = new ChangeExecutor<>(List.of(handler));

        // When
        List<Change> changes = List.of(change);
        List<ChangeResult<Change>> results = executor.execute(changes, false);

        // Then
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(changes, handler.capturedChanges);
    }

    @Test
    void shouldNotExecuteHandlerForUnsupportedType() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(ChangeType.DELETE);
        Change change = () -> ChangeType.ADD;
        ChangeExecutor<Change> executor = new ChangeExecutor<>(List.of(handler));

        // When
        List<Change> changes = List.of(change);
        List<ChangeResult<Change>> results = executor.execute(changes, false);

        // Then
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    void shouldAlwaysReturnNoneChanges() {
        // Given
        TestChangeHandler handler = new TestChangeHandler(ChangeType.NONE);
        Change change = () -> ChangeType.NONE;
        ChangeExecutor<Change> executor = new ChangeExecutor<>(List.of(handler));

        // When
        List<Change> changes = List.of(change);
        List<ChangeResult<Change>> results = executor.execute(changes, false);

        // Then
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(changes, handler.capturedChanges);
    }


    public static class TestChangeHandler implements ChangeHandler<Change> {

        final ChangeType type;

        List<Change> capturedChanges = new ArrayList<>();

        public TestChangeHandler(ChangeType type) {
            this.type = type;
        }

        @Override
        public Set<ChangeType> supportedChangeTypes() {
            return Set.of(type);
        }

        @Override
        public List<ChangeResponse<Change>> apply(@NotNull List<Change> changes) {
            this.capturedChanges.addAll(changes);
            return changes.stream().map(ChangeResponse::new).toList();
        }

        @Override
        public ChangeDescription getDescriptionFor(@NotNull Change change) {
            return type::name;
        }
    }
}