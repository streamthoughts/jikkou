/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

@JsonPropertyOrder({
        "name",
        "title",
        "category",
        "provider",
        "enabled",
})
@Reflectable
public record ApiExtensionSummary(@JsonProperty("name") String name,
                                  @JsonProperty("title") String title,
                                  @JsonProperty("category") String category,
                                  @JsonProperty("provider") String provider,
                                  @JsonProperty("enabled") boolean enabled) {

    @ConstructorProperties({
            "name",
            "title",
            "category",
            "provider",
            "enabled",
    })
    public ApiExtensionSummary {

    }
}
