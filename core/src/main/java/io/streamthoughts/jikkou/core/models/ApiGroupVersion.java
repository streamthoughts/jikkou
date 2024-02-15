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
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * ApiGroupVersion
 * @param groupVersion the combined api {group}/{version}.
 * @param version      the version.
 */
@JsonPropertyOrder({
        "groupVersion",
        "version",
        "metadata"
})
@Reflectable
public record ApiGroupVersion(@JsonProperty("groupVersion") @NotNull String groupVersion,
                              @JsonProperty("version") @NotNull String version,
                              @JsonProperty("metadata") Map<String, Object> metadata) {

    @ConstructorProperties({
            "groupVersion",
            "version",
            "metadata"
    })
    public ApiGroupVersion {
    }

    /**
     * Creates a new {@link ApiGroupVersion} instance.
     *
     * @param groupVersion the combined api {group}/{version}.
     * @param version      the version.
     */
    public ApiGroupVersion(@NotNull String groupVersion,
                           @NotNull String version) {
        this(groupVersion, version, Collections.emptyMap());
    }
}
