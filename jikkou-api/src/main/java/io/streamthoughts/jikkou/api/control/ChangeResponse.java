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

import io.vavr.control.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the response
 *
 * @param <T> type of the change.
 */
public final class ChangeResponse<T extends Change> {

    private final T change;

    private final List<CompletableFuture<?>> results;

    /**
     * Creates a new {@link ChangeResponse} for the given change.
     *
     * @param change    the change to attached to this response.
     */
    public ChangeResponse(T change) {
        this(change, new ArrayList<>());
    }

    /**
     * Creates a new {@link ChangeResponse} for the given change.
     *
     * @param change    the change to attached to this response.
     * @param result    the result to attached to this response;
     */
    public ChangeResponse(T change, CompletableFuture<?> result) {
        this(change, List.of(result));
    }

    public ChangeResponse(T change, List<? extends CompletableFuture<?>> results) {
        this.change = change;
        this.results = new ArrayList<>(results);
    }

    /**
     * Add a result to this response.
     *
     * @param result
     */
    public void addResult(CompletableFuture<Void> result) {
        this.results.add(result);
    }

    public T getChange() {
        return change;
    }

    /**
     * Gets the all {@link ChangeMetadata}.
     *
     * @return  the {@link CompletableFuture}.
     */
    public CompletableFuture<? extends List<ChangeMetadata>> getResults() {
        List<io.vavr.concurrent.Future<Option<ChangeMetadata>>> futures = results
                .stream()
                .map(io.vavr.concurrent.Future::fromJavaFuture)
                .map(f -> f.map(it -> Option.of(new ChangeMetadata())))
                .map(f -> f.recover(it -> Option.of(new ChangeMetadata(it))))
                .toList();

        return io.vavr.concurrent.Future.fold(futures,
                        new ArrayList<ChangeMetadata>(),
                        (list, option) -> {
                            option.peek(list::add);
                            return list;
                        })
                .toCompletableFuture();
    }
}
