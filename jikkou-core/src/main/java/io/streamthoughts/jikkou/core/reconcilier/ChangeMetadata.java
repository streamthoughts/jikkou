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
package io.streamthoughts.jikkou.core.reconcilier;

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
