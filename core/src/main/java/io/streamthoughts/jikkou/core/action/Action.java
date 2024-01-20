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
package io.streamthoughts.jikkou.core.action;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.HasMetadataAcceptable;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for executing a one-shot action on a specific type of resources.
 *
 * @param <T> The type of the resource.
 */
@Category(ExtensionCategory.ACTION)
public interface Action<T extends HasMetadata> extends HasMetadataAcceptable, Extension {

    /**
     * Executes the action.
     *
     * @param configuration The configuration
     * @return The ExecutionResultSet
     */
    @NotNull ExecutionResultSet<T> execute(@NotNull Configuration configuration);
}
