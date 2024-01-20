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
package io.streamthoughts.jikkou.core.extension;

import java.util.Optional;
import java.util.stream.Stream;

/**
 *
 * @param <T>   type of the extension.
 */
public interface Qualifier<T> {

    default boolean contains(final Qualifier<?> qualifier) {
        return equals(qualifier);
    }

    /**
     * Filters all the given candidates.
     *
     * @param extensionType     the type of extension.
     * @param candidates        all the descriptor candidates.
     * @return                  a stream containing only the candidates matching this qualifier.
     */
    Stream<ExtensionDescriptor<T>> filter(final Class<T> extensionType,
                                          final Stream<ExtensionDescriptor<T>> candidates);

    /**
     * Finds the first candidate matching this qualifier.
     *
     * @param extensionType     the type of extension.
     * @param candidates        all the descriptor candidates.
     * @return                  a stream containing only the candidates matching this qualifier.
     */
    default Optional<ExtensionDescriptor<T>> findFirst(final Class<T> extensionType,
                                                       final Stream<ExtensionDescriptor<T>> candidates) {
        return filter(extensionType, candidates).findFirst();
    }
}
