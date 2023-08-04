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
package io.streamthoughts.jikkou.api.transform;

import io.streamthoughts.jikkou.api.extensions.ResourceInterceptorDecorator;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This class can be used to decorate a transformation with a different name and priority.
 *
 * @param <T> type of resources accepted by the transformation.
 */
public class ResourceTransformationDecorator<T extends HasMetadata>
        extends ResourceInterceptorDecorator<ResourceTransformation<T>, ResourceTransformationDecorator<T>>
        implements ResourceTransformation<T> {


    /**
     * Creates a new {@link ResourceTransformationDecorator} instance.
     *
     * @param transformation the transformation to delegate, must not be {@code null}.
     */
    public ResourceTransformationDecorator(final @NotNull ResourceTransformation<T> transformation) {
       super(transformation);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @NotNull Optional<T> transform(@NotNull T toTransform, @NotNull HasItems resources) {
        return extension.transform(toTransform, resources);
    }
}
