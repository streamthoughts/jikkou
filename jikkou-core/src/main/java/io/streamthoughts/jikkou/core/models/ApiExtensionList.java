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
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.List;
import javax.validation.constraints.NotNull;

@ApiVersion(ApiExtensionList.API_VERSION)
@Kind(ApiExtensionList.KIND)
@JsonPropertyOrder({
        "kind",
        "apiVersion",
        "extensions"
})
@Names(
        plural = "extensions",
        singular = "extension"
)
@JsonDeserialize
@Reflectable
public record ApiExtensionList(@JsonProperty("kind") @NotNull String kind,
                               @JsonProperty("apiVersion") @NotNull String apiVersion,
                               @JsonProperty("extensions") @NotNull List<ApiExtensionSummary> extensions) {

    public static final String API_VERSION = "core.jikkou.io/v1";
    public static final String KIND = "ApiExtensionList";

    @ConstructorProperties({
            "kind",
            "apiVersion",
            "extensions"
    })
    public ApiExtensionList {
    }

    public ApiExtensionList(@NotNull List<ApiExtensionSummary> extensions) {
        this(
                KIND,
                API_VERSION,
                extensions
        );
    }
}