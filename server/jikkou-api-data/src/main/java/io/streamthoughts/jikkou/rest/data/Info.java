/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;

/**
 * Information about the Jikkou API server.
 *
 * @param version        The version of the Jikkou API server.
 * @param buildTimestamp The build-timestamp of the Jikkou API server.
 * @param commitId       The Git Commit ID of the Jikkou API server.
 */
@JsonPropertyOrder({
        "version",
        "build_time",
        "commit_id"
})
@Reflectable
public record Info(@JsonProperty("version") String version,
                   @JsonProperty("build_time") String buildTimestamp,
                   @JsonProperty("commit_id") String commitId) {

    @ConstructorProperties({
            "version",
            "build_time",
            "commit_id"
    })
    public Info {

    }

    public static Info empty() {
        return new Info(null, null, null);
    }

}
