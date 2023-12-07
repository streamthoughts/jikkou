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
package io.streamthoughts.jikkou.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Transient;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import java.beans.ConstructorProperties;
import java.time.Instant;

/**
 * SystemTimeResource.
 *
 * @param kind       The resource Kind.
 * @param apiVersion The resource API Version.
 * @param epochMilli The unix epoch milliseconds.
 * @param dateTime   The ISO-8601 date-time.
 */
@Kind(SystemTimeResource.KIND)
@ApiVersion(SystemTimeResource.API_VERSION)
@Transient
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "epochMilli",
        "dateTime"
})
public record SystemTimeResource(@JsonProperty("kind") String kind,
                                 @JsonProperty("apiVersion") String apiVersion,
                                 @JsonProperty("epochMilli") long epochMilli,
                                 @JsonProperty("dateTime") String dateTime) implements HasMetadata {

    public static final String KIND = "SystemTime";
    public static final String API_VERSION = "core.jikkou.io/v1";

    public static SystemTimeResource now() {
        Instant now = Instant.now();
        return new SystemTimeResource(now.toEpochMilli(), now.toString());
    }

    /**
     * Creates a new {@link SystemTimeResource} instance.
     *
     * @param kind       The resource Kind.
     * @param apiVersion The resource API Version.
     * @param epochMilli The unix epoch milliseconds.
     * @param dateTime   The ISO-8601 date-time.
     */
    @ConstructorProperties({
            "epochMilli",
            "dateTime"
    })
    public SystemTimeResource {
    }

    /**
     * Creates a new {@link SystemTimeResource} instance.
     *
     * @param epochMilli The unix epoch milliseconds.
     * @param dateTime   The ISO-8601 date-time.
     */
    public SystemTimeResource(
            long epochMilli,
            String dateTime
    ) {
        this(KIND, API_VERSION, epochMilli, dateTime);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ObjectMeta getMetadata() {
        return null;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasMetadata withMetadata(ObjectMeta metadata) {
        return null;
    }
}
