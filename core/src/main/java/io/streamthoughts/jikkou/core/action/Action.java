/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
