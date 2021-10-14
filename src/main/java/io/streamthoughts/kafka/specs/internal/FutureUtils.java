/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.kafka.specs.internal;

import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.TopicChange;
import io.streamthoughts.kafka.specs.operation.Operation;
import io.streamthoughts.kafka.specs.operation.TopicOperation;
import org.apache.kafka.common.KafkaFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FutureUtils {

    public static <T> CompletableFuture<T> toCompletableFuture(final KafkaFuture<T> future) {
        return future.toCompletionStage().toCompletableFuture();
    }

    public static CompletableFuture<Void> toVoidCompletableFuture(final KafkaFuture<?> future) {
        return toCompletableFuture(future).thenApply(p -> null);
    }

    public static <T extends Change<T>> CompletableFuture<OperationResult<T>> makeCompletableFuture(
            final Future<Void> future,
            final T change,
            final Operation<T> operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                future.get();
                return OperationResult.changed(change, operation.getDescriptionFor(change));
            } catch (InterruptedException | ExecutionException e) {
                return OperationResult.failed(change, operation.getDescriptionFor(change), e);
            }
        });
    }
}
