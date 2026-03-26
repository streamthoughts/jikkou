/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

@JsonPropertyOrder({
        "kind",
        "group",
        "apiVersion",
        "description",
})
@Reflectable
public record ApiResourceSummary(@JsonProperty("kind") String kind,
                                 @JsonProperty("group") String group,
                                 @JsonProperty("apiVersion") String apiVersion,
                                 @JsonProperty("description") String description) {

    @ConstructorProperties({
            "kind",
            "group",
            "apiVersion",
            "description",
    })
    public ApiResourceSummary {

    }
}
