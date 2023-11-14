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
 * Interface to convert resources from one type to another.
 *
 * @param <T>  – the type of the input resources to convert.
 * @param <TT> – the type of the result resources.
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