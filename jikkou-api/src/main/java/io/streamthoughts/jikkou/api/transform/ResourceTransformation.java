/*
 * Copyright 2021 The original authors
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

import io.streamthoughts.jikkou.annotation.ExtensionType;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.extensions.ResourceInterceptor;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * This interface is used to transform a resource.
 */
@Evolving
@ExtensionType("Transformation")
public interface ResourceTransformation<T extends HasMetadata> extends ResourceInterceptor {

    /**
     * Applies this transformation on the given {@link HasMetadata} object.
     *
     * @param toTransform  the {@link HasMetadata} to be transformed.
     * @param resources    the {@link ResourceListObject} involved in the current operation.
     *
     * @return            the list of resources resulting from that transformation.
     */
    default @NotNull Optional<T> transform(@NotNull T toTransform,
                                           @NotNull HasItems resources,
                                           @NotNull ReconciliationContext context) {
        return transform(toTransform, resources);
    }

    /**
     * Applies this transformation on the given {@link HasMetadata} object.
     *
     * @param toTransform  the {@link HasMetadata} to be transformed.
     * @param resources    the {@link ResourceListObject} involved in the current operation.
     *
     * @return            the list of resources resulting from that transformation.
     */
    default @NotNull Optional<T> transform(@NotNull T toTransform, @NotNull HasItems resources) {
        throw new UnsupportedOperationException();
    }
}
