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

public final class EnabledQualifier<T> implements Qualifier<T> {

    private final boolean enabled;

    /**
     * Creates a new {@link EnabledQualifier} instance.
     */
    EnabledQualifier() {
        this( true);
    }

    /**
     * Creates a new {@link EnabledQualifier} instance.
     *
     * @param enabled  specified if the extension should be enabled.
     */
    EnabledQualifier(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stream<ExtensionDescriptor<T>> filter(final Class<T> extensionType,
                                                 final Stream<ExtensionDescriptor<T>> candidates) {
        return candidates.filter(this::matches);
    }

    private boolean matches(final ExtensionDescriptor<T> descriptor) {
        return enabled == descriptor.isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnabledQualifier<?> that = (EnabledQualifier<?>) o;
        return enabled == that.enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "@Enabled(" + enabled + ")";
    }
}
