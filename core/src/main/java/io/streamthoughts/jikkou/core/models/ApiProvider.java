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
import javax.validation.constraints.NotNull;

@ApiVersion(ApiProvider.API_VERSION)
@Kind(ApiProvider.KIND)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "spec"
})
@Names(
        plural = "providers",
        singular = "provider"
)
@JsonDeserialize
@Reflectable
public record ApiProvider(@JsonProperty("kind") @NotNull String kind,
                          @JsonProperty("apiVersion") @NotNull String apiVersion,
                          @JsonProperty("spec") @NotNull ApiProviderSpec spec) {

    public static final String API_VERSION = "core.jikkou.io/v1";
    public static final String KIND = "ApiProvider";

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "spec"
    })
    public ApiProvider {
    }

    public ApiProvider(@NotNull ApiProviderSpec spec) {
        this(
                KIND,
                API_VERSION,
                spec
        );
    }
}
