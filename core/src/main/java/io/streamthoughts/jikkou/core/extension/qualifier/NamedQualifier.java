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
package io.streamthoughts.jikkou.core.extension.qualifier;

import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Qualifier;
import java.util.Objects;
import java.util.stream.Stream;

public final class NamedQualifier<T> implements Qualifier<T> {

    private final String name;
    private final boolean equals;

    /**
     * Creates a new {@link NamedQualifier} instance.
     *
     * @param name  the component name.
     */
    NamedQualifier(final String name) {
        this(name, true);
    }

    /**
     * Creates a new {@link NamedQualifier} instance.
     *
     * @param name  the component name.
     */
    NamedQualifier(final String name, final Boolean equals) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
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

    protected boolean matches(final ExtensionDescriptor<T> descriptor) {
        final String name = descriptor.name();
        return equals == name.equalsIgnoreCase(this.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NamedQualifier)) return false;
        NamedQualifier<?> that = (NamedQualifier<?>) o;
        return name.equals(that.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "@Named(" + name + ")";
    }
}
