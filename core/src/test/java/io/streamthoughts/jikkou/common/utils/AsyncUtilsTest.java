/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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