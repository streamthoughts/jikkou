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
package io.streamthoughts.jikkou.core.resource.converter;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability.Evolving;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for converting resources of type {@code I} to resources of type {@code O}.
 *
 * @param <T> – the type of the input resources to convert.
 * @param <R> – the type of the result resources.
 */
@Evolving
@Reflectable
public interface ResourceConverter<T extends HasMetadata, R extends HasMetadata> {

    /**
     * Converts the list of resources of type {@code T} to one or more resources of type {@code R}.
     *
     * @param resources   the list of resources to convert.
     * @return            the list of resources resulting from that conversion.
     */
    @NotNull List<R> convertFrom(@NotNull List<T> resources);

    /**
     * Converts the list of resources of type {@code R} to one or more resources of type {@code T}.
     *
     * @param resources   the list of resources to convert.
     * @return            the list of resources resulting from that conversion.
     */
    @NotNull List<T> convertTo(@NotNull List<R> resources);
}