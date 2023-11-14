/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
