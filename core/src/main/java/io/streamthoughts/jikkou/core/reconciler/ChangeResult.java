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
package io.streamthoughts.jikkou.core.reconciler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.common.utils.Enums;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Interface to represent a change result.
 */
@JsonDeserialize(as = DefaultChangeResult.class)
public interface ChangeResult {

    /**
     * Factory method for building a new {@link ChangeResult} that doesn't result in cluster resource changes.
     *
     * @param change      the operation result.
     * @param description the operation result description.
     * @return a new {@link ChangeResult}.
     */
    static ChangeResult ok(final ResourceChange change,
                           final TextDescription description) {
        return new DefaultChangeResult(Instant.now(), Status.OK, change, description, null);
    }

    /**
     * Factory method for building a new {@link ChangeResult} that do result in cluster resource changes.
     *
     * @param change      the operation result.
     * @param description the operation result description.
     * @return a new {@link ChangeResult}.
     */
    static ChangeResult changed(final ResourceChange change,
                                final TextDescription description) {
        return new DefaultChangeResult(Instant.now(), Status.CHANGED, change, description, null);
    }

    /**
     * Factory method for building a new {@link ChangeResult}  that failed with the specified exception.
     *
     * @param change      the resource change.
     * @param description the operation description.
     * @param errors      the errors.
     * @return a new {@link ChangeResult}.
     */
    static ChangeResult failed(final ResourceChange change,
                               final TextDescription description,
                               final List<ChangeError> errors) {
        return new DefaultChangeResult(Instant.now(), Status.FAILED, change, description, errors);
    }

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
            return Enums.getForNameIgnoreCase(str, ChangeResult.Status.class);
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

    /**
     * Gets the epoch time when then changed ended.
     * @return the epoch time.
     */
    Instant end();

    /**
     * Gets the change.
     *
     * @return the change resource.
     */
    ResourceChange change();

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
    TextDescription description();
}
