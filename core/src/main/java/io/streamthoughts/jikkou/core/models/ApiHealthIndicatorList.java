/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import javax.validation.constraints.NotNull;

@ApiVersion(ApiHealthIndicatorList.API_VERSION)
@Kind(ApiHealthIndicatorList.KIND)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "indicators"
})
@Reflectable
@Names(
    plural = "healths",
    singular = "health"
)
@JsonDeserialize
public record ApiHealthIndicatorList(@JsonProperty("kind") @NotNull String kind,
                                     @JsonProperty("apiVersion") @NotNull String apiVersion,
                                     @JsonProperty("indicators") @NotNull List<ApiHealthIndicator> indicators)
        implements Resource {

    public static final String API_VERSION = "core.jikkou.io/v1";
    public static final String KIND = "ApiHealthIndicatorList";

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "indicators"
    })
    public ApiHealthIndicatorList {
    }

    public ApiHealthIndicatorList(@NotNull List<ApiHealthIndicator> indicators) {
        this(
                KIND,
                API_VERSION,
                indicators
        );
    }
}