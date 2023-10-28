/*
 * Copyright 2020 The original authors
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.common.utils.Time;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import java.io.Serializable;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Represents a serializable result of the execution of a change.
 *
 * @param <T> the type of the change.
 * @see Change
 * @see ChangeResponse
 * @see DefaultChangeExecutor
 */
@Reflectable
@JsonPropertyOrder({
        "status",
        "changed",
        "failed",
        "end",
        "data",
        "error"
})
public final class DefaultChangeResult<T extends Change> implements Serializable, ChangeResult<T> {

    /**
     * Static method to build a new {@link DefaultChangeResult} that doesn't result in cluster resource changes.
     *
     * @param resource    the operation result.
     * @param description the operation result description.
     * @param <T>         the operation result-type.
     * @return a new {@link DefaultChangeResult}.
     */
    public static <T extends Change> ChangeResult<T> ok(final HasMetadataChange<T> resource,
                                                        final ChangeDescription description) {
        return new DefaultChangeResult<>(Status.OK, resource, description);
    }

    /**
     * Static method to build a new {@link DefaultChangeResult} that do result in cluster resource changes.
     *
     * @param resource    the operation result.
     * @param description the operation result description.
     * @param <T>         the operation result-type.
     * @return a new {@link DefaultChangeResult}.
     */
    public static <T extends Change> ChangeResult<T> changed(final HasMetadataChange<T> resource,
                                                             final ChangeDescription description) {
        return new DefaultChangeResult<>(Status.CHANGED, resource, description);
    }

    /**
     * Static method to build a new {@link DefaultChangeResult}  that failed with the specified exception.
     *
     * @param data        the operation result.
     * @param description the operation result description.
     * @param errors      the errors.
     * @param <T>         the operation result-type.
     * @return a new {@link DefaultChangeResult}.
     */
    public static <T extends Change> ChangeResult<T> failed(final HasMetadataChange<T> data,
                                                            final ChangeDescription description,
                                                            final List<ChangeError> errors) {
        return new DefaultChangeResult<>(Status.FAILED, data, description, errors);
    }

    private final long end;
    private final HasMetadataChange<T> data;
    private final List<ChangeError> errors;
    private final Status status;
    private transient final ChangeDescription description;

    /**
     * Creates a new {@link DefaultChangeResult} instance.
     *
     * @param status      the change execution status.
     * @param data        the resource on which the change was applied.
     * @param description the description of the change.
     */
    private DefaultChangeResult(final Status status,
                                final HasMetadataChange<T> data,
                                final ChangeDescription description) {
        this(status, data, description, null);
    }

    /**
     * Creates a new {@link DefaultChangeResult} instance.
     *
     * @param status      the change execution status.
     * @param data        the resource on which the change was applied.
     * @param description the description of the change.
     * @param errors      the change execution errors.
     */
    private DefaultChangeResult(final Status status,
                                final HasMetadataChange<T> data,
                                final ChangeDescription description,
                                final List<ChangeError> errors) {
        this(status, data, description, errors, Time.SYSTEM.milliseconds());
    }

    /**
     * Creates a new {@link DefaultChangeResult} instance.
     *
     * @param status      the change execution status.
     * @param data        the data of the change.
     * @param description the description of the change.
     * @param errors      the change execution errors.
     */
    private DefaultChangeResult(@NotNull final Status status,
                                @NotNull final HasMetadataChange<T> data,
                                @NotNull final ChangeDescription description,
                                @Nullable final List<ChangeError> errors,
                                final long end) {
        this.status = status;
        this.data = data;
        this.end = end;
        this.errors = errors;
        this.description = description;
    }

    @Override
    @JsonProperty
    public boolean isChanged() {
        return status == Status.CHANGED;
    }

    @Override
    @JsonProperty
    public boolean isFailed() {
        return status == Status.FAILED;
    }

    @Override
    @JsonProperty
    public long end() {
        return end;
    }

    @Override
    @JsonProperty
    public HasMetadataChange<T> data() {
        return data;
    }

    @Override
    @JsonProperty
    public List<ChangeError> errors() {
        return errors;
    }

    @Override
    @JsonProperty
    public Status status() {
        return this.status;
    }

    @Override
    @JsonIgnore
    public ChangeDescription description() {
        return this.description;
    }

    @Override
    public String toString() {
        return "ChangeResult{" +
                ", end=" + end +
                ", data=" + data +
                ", errors=" + errors +
                ", status=" + status +
                ", description=" + description +
                '}';
    }
}