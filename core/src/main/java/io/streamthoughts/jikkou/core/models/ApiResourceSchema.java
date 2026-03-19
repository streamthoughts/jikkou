/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

/**
 * Represents the JSON Schema for a resource type.
 *
 * @param apiVersion the API version of the resource.
 * @param kind       the kind of the resource.
 * @param schema     the JSON Schema.
 * @since 0.38.0
 */
@Reflectable
@JsonPropertyOrder({"apiVersion", "kind", "schema"})
public record ApiResourceSchema(
        @JsonProperty("apiVersion") String apiVersion,
        @JsonProperty("kind") String kind,
        @JsonProperty("schema") JsonNode schema
) {}
