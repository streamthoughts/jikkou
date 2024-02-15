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
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import javax.validation.constraints.NotNull;

@ApiVersion(ApiGroupList.API_VERSION)
@Kind(ApiGroupList.KIND)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "groups"
})
@Reflectable
@JsonDeserialize
public record ApiGroupList(@JsonProperty("kind") @NotNull String kind,
                           @JsonProperty("apiVersion") @NotNull String apiVersion,
                           @JsonProperty("groups") @NotNull List<ApiGroup> groups) implements Resource {

    public static final String API_VERSION = "v1";
    public static final String KIND = "ApiGroupList";

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "groups"
    })
    public ApiGroupList {}

    public ApiGroupList(@NotNull List<ApiGroup> groups) {
        this(
                KIND,
                API_VERSION,
                groups
        );
    }
}
