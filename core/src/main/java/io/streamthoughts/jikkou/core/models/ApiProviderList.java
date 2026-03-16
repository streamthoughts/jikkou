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

@ApiVersion(ApiProviderList.API_VERSION)
@Kind(ApiProviderList.KIND)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "providers"
})
@Names(
        plural = "providers",
        singular = "provider"
)
@JsonDeserialize
@Reflectable
public record ApiProviderList(@JsonProperty("kind") @NotNull String kind,
                              @JsonProperty("apiVersion") @NotNull String apiVersion,
                              @JsonProperty("providers") @NotNull List<ApiProviderSummary> providers) implements Resource {

    public static final String API_VERSION = "core.jikkou.io/v1";
    public static final String KIND = "ApiProviderList";

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "providers"
    })
    public ApiProviderList {
    }

    public ApiProviderList(@NotNull List<ApiProviderSummary> providers) {
        this(
                KIND,
                API_VERSION,
                providers
        );
    }
}
