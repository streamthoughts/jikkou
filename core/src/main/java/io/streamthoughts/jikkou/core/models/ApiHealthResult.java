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
