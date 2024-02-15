/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.io;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import io.streamthoughts.jikkou.core.models.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
@InterfaceStability.Evolving
public interface ResourceTypeResolver {

    /**
     * Gets the target type into which the specified {@link JsonNode} can be deserialized.
     *
     * @param node  the {@link JsonNode}.
     * @return      the class type into which to deserialize the {@link JsonNode},
     *              or {@code null} if no type can be resolved.
     */
    @Nullable Class<? extends Resource> resolvesType(@NotNull JsonNode node);
}
