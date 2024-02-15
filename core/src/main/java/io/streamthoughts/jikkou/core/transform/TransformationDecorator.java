/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.resource.InterceptorDecorator;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This class can be used to decorate a transformation with a different name and priority.
 *
 * @param <T> type of resources accepted by the transformation.
 */
public class TransformationDecorator<T extends HasMetadata>
        extends InterceptorDecorator<Transformation<T>, TransformationDecorator<T>>
        implements Transformation<T> {


    /**
     * Creates a new {@link TransformationDecorator} instance.
     *
     * @param transformation the transformation to delegate, must not be {@code null}.
     */
    public TransformationDecorator(final @NotNull Transformation<T> transformation) {
       super(transformation);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<T> transform(@NotNull T resource,
                                          @NotNull HasItems resources,
                                          @NotNull ReconciliationContext context) {
        return extension.transform(resource, resources, context);
    }
}
