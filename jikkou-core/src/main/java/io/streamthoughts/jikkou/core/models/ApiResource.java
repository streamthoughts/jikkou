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
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * ApiResource.
 *
 * @param name          the name of the resource.
 * @param kind          the Kind of the resource.
 * @param singularName  the singular name of the resource.
 * @param shortNames    the short names of the resource.
 * @param description   the description of the resource.
 * @param verbs         the verbs supported by the resource.
 * @param metadata      the resource metadata.
 */
@JsonPropertyOrder( {
        "name",
        "kind",
        "singularName",
        "shortNames",
        "description",
        "verbs",
        "metadata"}
)
public record ApiResource(
        @JsonProperty("name") String name,
        @JsonProperty("kind") String kind,
        @JsonProperty("singularName") String singularName,
        @JsonProperty("shortNames") Set<String> shortNames,
        @JsonProperty("description") String description,
        @JsonProperty("verbs") Set<String> verbs,
        @JsonProperty("metadata") Map<String, Object> metadata) {

    @ConstructorProperties({
            "name",
            "kind",
            "singularName",
            "shortNames",
            "description",
            "verbs",
            "metadata"})
    public ApiResource {
    }

    public ApiResource(
            String name,
            String kind,
            String singularName,
            Set<String> shortNames,
            String description,
            Set<String> verbs) {
        this(name, kind, singularName, shortNames, description, verbs, Collections.emptyMap());

    }
}
