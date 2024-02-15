/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.health.Health;
import io.streamthoughts.jikkou.core.health.HealthStatus;
import jakarta.validation.constraints.NotNull;
import java.beans.ConstructorProperties;
import java.util.Map;

/**
 * Used to represent the result of a {@link io.streamthoughts.jikkou.core.health.HealthIndicator}.
 *
 * @param apiVersion The API version of this object.
 * @param kind       The Kind of this object.
 * @param name       The name of the health indicator.
 * @param status     The status of the health indicator.
 * @param details    The detailed information about the status.
 */
@ApiVersion(ApiHealthResult.API_VERSION)
@Kind(ApiHealthResult.KIND)
@JsonPropertyOrder({
        "apiVersion",
        "kind",
        "name",
        "status",
        "details"
})
@Reflectable
@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiHealthResult(@NotNull @JsonProperty("apiVersion") String apiVersion,
                              @NotNull @JsonProperty("kind") String kind,
                              @NotNull @JsonProperty("name") String name,
                              @NotNull @JsonProperty("status") HealthStatus status,
                              @JsonProperty("details") Map<String, Object> details) implements Resource {

    public static final String API_VERSION = "core.jikkou.io/v1";
    public static final String KIND = "ApiHealthResult";

    @ConstructorProperties({
            "apiVersion",
            "kind",
            "name",
            "status",
            "details"
    })
    public ApiHealthResult {

    }

    /**
     * Creates a new {@link ApiHealthResult} instance.
     *
     * @param health The health object.
     */
    public static ApiHealthResult from(Health health) {
        return new ApiHealthResult(
                API_VERSION,
                KIND,
                health.getName(),
                health.getStatus(),
                health.getDetails()
        );
    }
}
