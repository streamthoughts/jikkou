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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({
        "name",
        "type",
        "description",
        "tags",
        "externalDocs",
        "enabled",
        "options",
        "resources",
        "extensions"
})
@Reflectable
public record ApiProviderSpec(@JsonProperty("name") String name,
                              @JsonProperty("type") String type,
                              @JsonProperty("description") String description,
                              @JsonProperty("tags") List<String> tags,
                              @JsonProperty("externalDocs") String externalDocs,
                              @JsonProperty("enabled") boolean enabled,
                              @JsonProperty("options") List<ApiOptionSpec> options,
                              @JsonProperty("resources") List<ApiResourceSummary> resources,
                              @JsonProperty("extensions") List<ApiExtensionSummary> extensions) {

    @ConstructorProperties({
            "name",
            "type",
            "description",
            "tags",
            "externalDocs",
            "enabled",
            "options",
            "resources",
            "extensions"
    })
    public ApiProviderSpec {

    }

    @Override
    public List<String> tags() {
        return Optional.ofNullable(tags).orElse(Collections.emptyList());
    }

    @Override
    public List<ApiOptionSpec> options() {
        return Optional.ofNullable(options).orElse(Collections.emptyList());
    }

    @Override
    public List<ApiResourceSummary> resources() {
        return Optional.ofNullable(resources).orElse(Collections.emptyList());
    }

    @Override
    public List<ApiExtensionSummary> extensions() {
        return Optional.ofNullable(extensions).orElse(Collections.emptyList());
    }
}
