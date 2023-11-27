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
package io.streamthoughts.jikkou.common.utils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AsyncUtilsTest {

    @Test
    void shouldGetEmptyForFailedFuture() {
        // Given
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException());
        // When
        Optional<Object> optional = AsyncUtils.getValue(future);
        // Then
        Assertions.assertTrue(optional.isEmpty());
    }

    @Test
    void shouldGetObjectForCompletedFuture() {
        // Given
        CompletableFuture<Object> future = new CompletableFuture<>();
        Object value = new Object();
        future.complete(value);
        // When
        Optional<Object> optional = AsyncUtils.getValue(future);
        // Then
        Assertions.assertEquals(Optional.of(value), optional);
    }

    @Test
    void shouldGetNonEmptyExceptionForFailedCompletedFuture() {
        // Given
        CompletableFuture<Object> future = new CompletableFuture<>();
        RuntimeException ex = new RuntimeException();
        future.completeExceptionally(ex);
        // When
        Optional<Throwable> optional = AsyncUtils.getException(future);
        // Then
        Assertions.assertEquals(Optional.of(ex), optional);
    }

    @Test
    void shouldGetEmptyExceptionForCompletedFuture() {
        // Given
        CompletableFuture<Object> future = new CompletableFuture<>();
        future.complete(new Object());
        // When
        Optional<Throwable> optional = AsyncUtils.getException(future);
        // Then
        Assertions.assertTrue(optional.isEmpty());
    }
}