/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.converter;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.annotations.Category;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.Interceptor;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for converting resources from one type to another.
 *
 * @param <T>  The type of the input resources to convert.
 * @param <TT> The type of the result resources.
 */
@Evolving
@Reflectable
@Category(ExtensionCategory.CONVERTER)
public interface Converter<T extends HasMetadata, TT extends HasMetadata> extends Interceptor {

    /**
     * Checks whether this converter can accept the specified resource.
     * This method can be used in addition to {@link #canAccept(ResourceType)} to provide a finer filter.
     *
     * @param resource The resource.
     * @return {@code true} if the given resource is acceptable.
     */
    default boolean canAccept(Resource resource) {
        return true;
    }

    /**
     * Converts the specified resource of type {@code T} to one or more resources of type {@code TT}.
     *
     * @param resource The resource to be converted
     * @return the list of resources resulting from that conversion.
     */
    @NotNull List<TT> apply(@NotNull T resource);
}