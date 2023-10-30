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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.models.generics.GenericChangeResult;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * Interface to represent a change result.
 *
 * @param <T> the change-type.
 */
@JsonDeserialize(as = GenericChangeResult.class)
public interface ChangeResult<T extends Change> {

    /**
     * Execution status.
     */
    enum Status {
        /**
         * Execution of all changes was successfully
         **/
        CHANGED,
        /**
         * Execution did not apply any change
         **/
        OK,
        /**
         * Execution of one or more changes failed
         **/
        FAILED;

        @JsonCreator
        public static Status getForNameIgnoreCase(final @Nullable String str) {
            if (str == null) throw new IllegalArgumentException("Unsupported status 'null'");
            return Arrays.stream(Status.values())
                    .filter(e -> e.name().equals(str.toUpperCase(Locale.ROOT)))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported status '" + str + "'"));
        }
    }

    /**
     * Verify if the status of the change execution is 'changed'.
     *
     * @return {@code true} if {@link #status()} returns {@link Status#CHANGED}.
     */
    default boolean isChanged() {
        return status() == Status.CHANGED;
    }

    /**
     * Verify if the status of the change execution is 'failed'.
     *
     * @return {@code true} if {@link #status()} returns {@link Status#FAILED}.
     */
    default boolean isFailed() {
        return status() == Status.FAILED;
    }

    Long end();

    /**
     * Gets the change.
     *
     * @return the change resource.
     */
    HasMetadataChange<T> data();

    /**
     * Gets the list of errors.
     *
     * @return the list of error.
     */
    List<ChangeError> errors();

    /**
     * Gets the status of this execution.
     *
     * @return the status.
     */
    Status status();

    /**
     * Gets the description of this change.
     *
     * @return a change description.
     */
    ChangeDescription description();
}
