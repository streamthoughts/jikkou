/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.models.Resource;
import org.jetbrains.annotations.NotNull;

/**
 * Generates a JSON Schema for a given resource class.
 */
public interface ResourceSchemaGenerator {

    /**
     * Generates a JSON Schema for the specified resource class.
     *
     * @param resourceClass the resource class.
     * @return a {@link JsonNode} representing the JSON Schema.
     */
    JsonNode generate(@NotNull Class<? extends Resource> resourceClass);
}
