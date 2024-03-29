/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.action;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for creating new {@link ExecutionResult} objects.
 *
 * @param <T> type of the resource.
 */
public final class ExecutionResultBuilder<T extends HasMetadata> {

    private ExecutionResult<T> delegate = new GenericExecutionResult<>(null, null, null);

    ExecutionResultBuilder() {
    }

    /**
     * Sets the status of the execution result.
     *
     * @param status The status
     * @return {@code this}.
     */
    public ExecutionResultBuilder<T> status(ExecutionStatus status) {
        delegate = new GenericExecutionResult<>(status, delegate.errors(), delegate.data());
        return this;
    }

    /**
     * Adds the specified error to the execution result.
     *
     * @param error The ExecutionError.
     * @return {@code this}.
     */
    public ExecutionResultBuilder<T> error(ExecutionError error) {
        List<ExecutionError> allErrors = getAllErrors();
        allErrors.add(error);
        delegate = new GenericExecutionResult<>(delegate.status(), allErrors, delegate.data());
        return this;
    }

    /**
     * Adds the specified errors to the execution result.
     *
     * @param errors The ExecutionErrors.
     * @return {@code this}.
     */
    public ExecutionResultBuilder<T> errors(List<ExecutionError> errors) {
        List<ExecutionError> allErrors = getAllErrors();
        allErrors.addAll(errors);
        delegate = new GenericExecutionResult<>(delegate.status(), allErrors, delegate.data());
        return this;
    }

    @NotNull
    private ArrayList<ExecutionError> getAllErrors() {
        return new ArrayList<>(Optional.ofNullable(delegate.errors()).orElse(Collections.emptyList()));
    }

    /**
     * Sets the specified data of the execution result.
     *
     * @param data The data.
     * @return {@code this}.
     */
    public ExecutionResultBuilder<T> data(T data) {
        delegate = new GenericExecutionResult<>(delegate.status(), delegate.errors(), data);
        return this;
    }

    /**
     * Builds the ExecutionResult.
     *
     * @return The ExecutionResult.
     */
    public ExecutionResult<T> build() {
        return delegate;
    }
}
