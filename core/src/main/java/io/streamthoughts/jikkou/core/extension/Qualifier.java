/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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
