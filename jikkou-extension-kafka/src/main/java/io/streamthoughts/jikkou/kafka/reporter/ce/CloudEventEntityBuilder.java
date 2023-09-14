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
package io.streamthoughts.jikkou.kafka.reporter.ce;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class CloudEventEntityBuilder<T> extends CloudEventAttributes {

    private T data;
    private final List<CloudEventExtension> extensions;

    public static <T> CloudEventEntityBuilder<T> newBuilder() {
        return new CloudEventEntityBuilder<>();
    }

    /** Creates a new {@link CloudEventEntityBuilder} instance. */
    private CloudEventEntityBuilder() {
        super();
        this.extensions = new LinkedList<>();
    }

    /** @see CloudEventAttributes#id() */
    public CloudEventEntityBuilder<T> withId(final String id) {
        this.id = id;
        return this;
    }

    /** @see CloudEventAttributes#type() */
    public CloudEventEntityBuilder<T> withType(final String type) {
        this.type = type;
        return this;
    }

    /** @see CloudEventAttributes#source() */
    public CloudEventEntityBuilder<T> withSource(final String source) {
        this.source = source;
        return this;
    }

    /** @see CloudEventAttributes#source() */
    public CloudEventEntityBuilder<T> withSubject(final String subject) {
        this.subject = subject;
        return this;
    }

    /** @see CloudEventAttributes#specVersion() */
    public CloudEventEntityBuilder<T> withSpecVersion(final String specVersion) {
        this.specVersion = specVersion;
        return this;
    }

    /** @see CloudEventAttributes#time() */
    public CloudEventEntityBuilder<T> withTime(final ZonedDateTime time) {
        this.time = time;
        return this;
    }

    /** @see CloudEventAttributes#dataContentType() */
    public CloudEventEntityBuilder<T> withDataContentType(final String dataContentType) {
        this.dataContentType = dataContentType;
        return this;
    }

    public CloudEventEntityBuilder<T> withData(final T data) {
        this.data = data;
        return this;
    }

    public CloudEventEntityBuilder<T> withExtension(final CloudEventExtension extension) {
        this.extensions.add(extension);
        return this;
    }

    /**
     * Builds a new cloud event entity
     *
     * @return a new {@link CloudEventEntity} instance.
     */
    public CloudEventEntity<T> build() {
        return new CloudEventEntity<>(this, extensions, data);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CloudEventEntityBuilder<?> that = (CloudEventEntityBuilder<?>) o;
        return Objects.equals(data, that.data) && Objects.equals(extensions, that.extensions);
    }
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data, extensions);
    }
}
