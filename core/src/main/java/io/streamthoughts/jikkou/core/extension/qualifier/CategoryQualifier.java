/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.qualifier;

import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Qualifier;
import java.util.Objects;
import java.util.stream.Stream;

public final class CategoryQualifier<T> implements Qualifier<T> {

    private final ExtensionCategory category;
    private final boolean equals;

    /**
     * Creates a new {@link CategoryQualifier} instance.
     *
     * @param category  The extension category.
     */
    public CategoryQualifier(final ExtensionCategory category) {
        this(category, true);
    }

    /**
     * Creates a new {@link CategoryQualifier} instance.
     *
     * @param category  The extension category.
     */
    CategoryQualifier(final ExtensionCategory category, final Boolean equals) {
        this.category = Objects.requireNonNull(category, "name cannot be null");
        this.equals = equals;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<ExtensionDescriptor<T>> filter(final Class<T> componentType,
                                                 final Stream<ExtensionDescriptor<T>> candidates) {
        return candidates.filter(this::matches);
    }

    private boolean matches(final ExtensionDescriptor<T> descriptor) {
        final ExtensionCategory category = descriptor.category();
        return equals == category.equals(this.category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryQualifier)) return false;
        CategoryQualifier<?> that = (CategoryQualifier<?>) o;
        return category.equals(that.category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "@Category(" + category + ")";
    }
}
