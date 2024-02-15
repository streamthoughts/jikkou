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
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A validation constraint violation.
 *
 * @see Validation
 */
public class ValidationError {
    private final @Nullable String name;
    private final @Nullable HasMetadata resource;
    private final @NotNull String message;
    private final @NotNull Map<String, Object> details;

    /**
     * Creates a new {@link ValidationError} instance.
     *
     * @param name     the validation constraint name.
     * @param resource the original resource.
     * @param message  the error message.
     * @param details  the error details.
     */
    public ValidationError(@Nullable String name,
                           @Nullable HasMetadata resource,
                           @NotNull String message,
                           @NotNull Map<String, Object> details) {
        this.name = name;
        this.resource = resource;
        this.message = message;
        this.details = details;
    }

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

    public @Nullable String name() {
        return name;
    }

    public @Nullable HasMetadata resource() {
        return resource;
    }

    public @NotNull String message() {
        return message;
    }

    public @NotNull Map<String, Object> details() {
        return details;
    }
    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ValidationError) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.resource, that.resource) &&
                Objects.equals(this.message, that.message) &&
                Objects.equals(this.details, that.details);
    }
    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(name, resource, message, details);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "ValidationError[" +
                "name=" + name + ", " +
                "resource=" + resource + ", " +
                "message=" + message + ", " +
                "details=" + details + ']';
    }

}
