/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.transform;

import io.jikkou.common.annotation.InterfaceStability.Evolving;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.extension.ExtensionCategory;
import io.jikkou.core.extension.annotations.Category;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.resource.Interceptor;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for transforming or filtering resources. Transformations are executed on each resource.
 * The resources resulting from a transformation will not themselves be transformed.
 *
 * @param <T> The resource type supported by the transformation.
 */
@Evolving
@Category(ExtensionCategory.TRANSFORMATION)
public interface Transformation<T extends HasMetadata> extends Interceptor {

    /**
     * Executes the transformation on the specified {@link HasMetadata} object.
     *
     * @param resource  The {@link HasMetadata} to be transformed.
     * @param resources The {@link ResourceList} involved in the current operation.
     * @param context   The {@link ReconciliationContext}.
     * @return The list of resources resulting from the transformation.
     */
    @NotNull Optional<T> transform(@NotNull T resource,
                                   @NotNull HasItems resources,
                                   @NotNull ReconciliationContext context);
}
