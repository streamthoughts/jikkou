/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import io.streamthoughts.jikkou.common.utils.AsyncUtils;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the response
 */
public final class ChangeResponse<C extends ResourceChange> {

    private final C change;

    private final List<CompletableFuture<ChangeMetadata>> results;

    /**
     * Creates a new {@link ChangeResponse} for the given change.
     *
     * @param change the change to attached to this response.
     */
    public ChangeResponse(@NotNull C change) {
        this(change, new ArrayList<>());
    }

    /**
     * Creates a new {@link ChangeResponse} for the given change.
     *
     * @param change the change to attached to this response.
     * @param result the result to attached to this response;
     */
    public ChangeResponse(@NotNull C change,
                          @NotNull CompletableFuture<ChangeMetadata> result) {
        this(change, List.of(result));
    }

    /**
     * Creates a new {@link ChangeResponse} instance.
     *
     * @param change  the change object.
     * @param results the change results.
     */
    public ChangeResponse(@NotNull C change, @NotNull List<CompletableFuture<ChangeMetadata>> results) {
        this.change = change;
        this.results = new ArrayList<>(results);
    }

    /**
     * Add a result to this response.
     *
     * @param result The future change metadata.
     */
    public void addResult(CompletableFuture<ChangeMetadata> result) {
        this.results.add(result);
    }

    /**
     * Gets the change object.
     *
     * @return the change.
     */
    public C getChange() {
        return change;
    }

    /**
     * Gets the all change metadata objects.
     *
     * @return the {@link CompletableFuture}.
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

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeResponse<C> that = (ChangeResponse<C>) o;
        return Objects.equals(change, that.change) && Objects.equals(results, that.results);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(change, results);
    }
}
