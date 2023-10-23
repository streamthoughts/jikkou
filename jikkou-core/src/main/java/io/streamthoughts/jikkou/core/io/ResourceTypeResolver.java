/*
 * Copyright 2022 The original authors
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
