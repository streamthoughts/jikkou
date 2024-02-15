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

@ApiVersion(ApiExtensionList.API_VERSION)
@Kind(ApiExtensionList.KIND)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "extensions"
})
@Names(
        plural = "extensions",
        singular = "extension"
)
@JsonDeserialize
@Reflectable
public record ApiExtensionList(@JsonProperty("kind") @NotNull String kind,
                               @JsonProperty("apiVersion") @NotNull String apiVersion,
                               @JsonProperty("extensions") @NotNull List<ApiExtensionSummary> extensions) implements Resource {

    public static final String API_VERSION = "core.jikkou.io/v1";
    public static final String KIND = "ApiExtensionList";

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "extensions"
    })
    public ApiExtensionList {
    }

    public ApiExtensionList(@NotNull List<ApiExtensionSummary> extensions) {
        this(
                KIND,
                API_VERSION,
                extensions
        );
    }
}