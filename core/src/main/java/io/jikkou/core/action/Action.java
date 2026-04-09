/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.action;

import io.jikkou.core.annotation.Enabled;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.Extension;
import io.jikkou.core.extension.ExtensionCategory;
import io.jikkou.core.extension.annotations.Category;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasMetadataAcceptable;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for executing a one-shot action on a specific type of resources.
 *
 * @param <T> The type of the resource.
 */
@Enabled
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
