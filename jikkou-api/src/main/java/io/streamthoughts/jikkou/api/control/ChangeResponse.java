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

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the response
 *
 * @param <T> type of the change.
 */
public final class ChangeResponse<T extends Change> {

    private final T change;

    private final List<CompletableFuture<ChangeMetadata>> results;

    /**
     * Creates a new {@link ChangeResponse} for the given change.
     *
     * @param change    the change to attached to this response.
     */
    public ChangeResponse(@NotNull T change) {
        this(change, new ArrayList<>());
    }

    /**
     * Creates a new {@link ChangeResponse} for the given change.
     *
     * @param change    the change to attached to this response.
     * @param result    the result to attached to this response;
     */
    public ChangeResponse(@NotNull T change, @NotNull CompletableFuture<ChangeMetadata> result) {
        this(change, List.of(result));
    }

    /**
     * Creates a new {@link ChangeResponse} instance.
     *
     * @param change    the change object.
     * @param results   the change results.
     */
    public ChangeResponse(@NotNull T change, @NotNull List<CompletableFuture<ChangeMetadata>> results) {
        this.change = change;
        this.results = new ArrayList<>(results);
    }

    /**
     * Add a result to this response.
     *
     * @param result
     */
    public void addResult(CompletableFuture<ChangeMetadata> result) {
        this.results.add(result);
    }

    /**
     * Gets the change object.
     *
     * @return  the change.
     */
    public T getChange() {
        return change;
    }

    /**
     * Gets the all change metadata objects.
     *
     * @return  the {@link CompletableFuture}.
     */
    public CompletableFuture<List<ChangeMetadata>> getResults() {
        List<CompletableFuture<ChangeMetadata>> futures = results.stream()
                .map(f -> f.exceptionally(throwable -> {
                    if (throwable instanceof CompletionException completionException) {
                        return ChangeMetadata.of(completionException.getCause());
                    }
                    return ChangeMetadata.of(throwable);
                }))
                .collect(Collectors.toList());
        return AsyncUtils.waitForAll(futures);
    }
}
