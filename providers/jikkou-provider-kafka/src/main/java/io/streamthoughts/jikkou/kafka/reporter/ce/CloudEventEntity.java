/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reporter.ce;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonPropertyOrder({
        "attributes",
        "extensions",
        "data"
})
@Reflectable
public final class CloudEventEntity<T> {

    private final CloudEventAttributes attributes;

    private final List<CloudEventExtension> extensions;

    private final T data;

    /**
     * Creates a new {@link CloudEventEntity} instance.
     *
     * @param attributes the context event attributes.
     * @param extensions the extension attributes.
     * @param data the event data.
     */
    CloudEventEntity(
            @NotNull final CloudEventAttributes attributes,
            @NotNull final List<CloudEventExtension> extensions,
            @Nullable final T data) {
        this.attributes = Objects.requireNonNull(attributes, "attributes cannot be null");
        this.extensions = Objects.requireNonNull(extensions, "extensions cannot be null");
        this.data = data;
    }

    @JsonUnwrapped
    @JsonProperty
    public CloudEventAttributes attributes() {
        return attributes;
    }

    @JsonAnyGetter
    @JsonProperty
    public Map<String, Object> extensions() {
        return CloudEventExtension.marshal(extensions);
    }

    @JsonProperty
    public T data() {
        return data;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloudEventEntity<?>)) return false;
        CloudEventEntity that = (CloudEventEntity) o;
        return Objects.equals(attributes, that.attributes)
                && Objects.equals(extensions, that.extensions)
                && Objects.equals(data, that.data);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(attributes, extensions, data);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "CloudEventsEntity{"
                + "attributes="
                + attributes
                + ", extensions="
                + extensions
                + ", data="
                + data
                + '}';
    }
}
