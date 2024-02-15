/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
