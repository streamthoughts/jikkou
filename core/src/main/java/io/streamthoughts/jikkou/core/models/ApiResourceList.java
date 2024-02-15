/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * ApiResourceList.
 * 
 * @param kind          The Kind of the resources.
 * @param apiVersion    The API Version of the resources.
 * @param groupVersion  The API Group of the resources.
 * @param resources     The list of resources.
 */
@Kind("ApiResourceList")
@ApiVersion("v1")
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "groupVersion",
        "resources"
})
@Reflectable
@JsonDeserialize
public record ApiResourceList(@NotNull String kind,
                              @NotNull String apiVersion,
                              @NotNull String groupVersion,
                              @NotNull List<ApiResource> resources) implements Resource {

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "groupVersion",
            "resources"
    })
    public ApiResourceList {}

    public ApiResourceList(@NotNull String groupVersion,
                           @NotNull List<ApiResource> resources) {

        this(
                Resource.getKind(ApiResourceList.class),
                Resource.getApiVersion(ApiResourceList.class),
                groupVersion,
                resources
        );
    }
}
