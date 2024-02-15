/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the metadata attached to a change task operation result.
 */
public final class ChangeMetadata {

    public static ChangeMetadata empty() {
        return new ChangeMetadata(null);
    }

    public static ChangeMetadata of(Throwable error) {
        String message = String.format("%s: %s", error.getClass().getSimpleName(), error.getLocalizedMessage());
        return new ChangeMetadata(new ChangeError(message, null));
    }

    private final ChangeError error;

    /**
     * Creates a new {@link ChangeMetadata}.
     */
    public ChangeMetadata() {
        this(null);
    }

    /**
     * Creates a new {@link ChangeMetadata}.
     *
     * @param error the error.
     */
    public ChangeMetadata(@Nullable ChangeError error) {
        this.error = error;
    }

    public Optional<ChangeError> getError() {
        return Optional.ofNullable(error);
    }

    @Override
    public String toString() {
        return "ChangeMetadata{" +
                "error=" + error +
                '}';
    }
}
