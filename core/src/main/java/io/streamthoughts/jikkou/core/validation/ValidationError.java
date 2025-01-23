/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.validation;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.Collections;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A validation constraint violation.
 *
 * @see Validation
 */
public record ValidationError(
   @Nullable String name,
   @Nullable HasMetadata resource,
   @NotNull String message,
   @NotNull Map<String, Object> details
) {

    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param name    the validation constraint name.
     * @param message the error message.
     */
    public ValidationError(@NotNull String name, @NotNull String message) {
        this(name, null, message, Collections.emptyMap());
    }


    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param message the error message.
     */
    public ValidationError(@NotNull String message) {
        this(null, null, message, Collections.emptyMap());
    }

    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param resource the original resource.
     * @param message  the error message.
     */
    public ValidationError(@NotNull HasMetadata resource, @NotNull String message) {
        this(null, resource, message, Collections.emptyMap());
    }

    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param resource the original resource.
     * @param message  the error message.
     */
    public ValidationError(@NotNull String name, @NotNull HasMetadata resource, @NotNull String message) {
        this(name, resource, message, Collections.emptyMap());
    }

    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param message the error message.
     */
    public ValidationError(@NotNull String message, @NotNull Map<String, Object> details) {
        this(null, null, message, details);
    }
}
