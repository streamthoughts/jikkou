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
import io.streamthoughts.jikkou.core.extension.Example;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonPropertyOrder({
        "name",
        "title",
        "description",
        "examples",
        "category",
        "group",
        "description",
        "options",
        "resources"
})
@Reflectable
public record ApiExtensionSpec(@JsonProperty("name") String name,
                               @JsonProperty("title") String title,
                               @JsonProperty("description") String description,
                               @JsonProperty("examples") List<Example> examples,
                               @JsonProperty("category") String category,
                               @JsonProperty("provider") String provider,
                               @JsonProperty("options") List<ApiOptionSpec> options,
                               @JsonProperty("resources") List<ResourceType> resources) {

    @ConstructorProperties({
            "name",
            "title",
            "description",
            "examples",
            "category",
            "provider",
            "description",
            "options",
            "resources"
    })
    public ApiExtensionSpec {

    }

    @Override
    public List<Example> examples() {
        return Optional.ofNullable(examples).orElse(Collections.emptyList());
    }
}
