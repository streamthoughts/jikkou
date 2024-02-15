/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Qualifier;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class AnyQualifier<T> implements Qualifier<T> {

    private final List<Qualifier<T>> qualifiers;

    /**
     * Creates a new {@link AnyQualifier} instance.
     *
     * @param qualifiers    the list of {@link Qualifier}.
     */
    public AnyQualifier(final List<Qualifier<T>> qualifiers) {
        this.qualifiers = qualifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<ExtensionDescriptor<T>> filter(final Class<T> extensionType,
                                                 final Stream<ExtensionDescriptor<T>> candidates) {
        final List<ExtensionDescriptor<T>> listCandidates = candidates.toList();
        return qualifiers.stream()
              .flatMap(qualifier -> qualifier.filter(extensionType, listCandidates.stream()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnyQualifier)) return false;
        AnyQualifier<?> that = (AnyQualifier<?>) o;
        return Objects.equals(qualifiers, that.qualifiers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(qualifiers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return qualifiers.stream().map(Object::toString).collect(Collectors.joining(" and "));
    }
}
