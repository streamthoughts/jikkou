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

import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.annotation.HandledResource;
import io.streamthoughts.jikkou.core.extension.ExtensionAttribute;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Qualifier;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public final class AcceptedResourceQualifier<T> implements Qualifier<T> {

    private static final String METADATA_ATTRIBUTE_NAME = HandledResource.class
            .getSimpleName()
            .toLowerCase(Locale.ROOT);

    private final ResourceType type;
    private final boolean equals;

    /**
     * Creates a new {@link AcceptedResourceQualifier} instance.
     *
     * @param type  the resource type accepted by the extension.
     */
    AcceptedResourceQualifier(final ResourceType type) {
        this(type, true);
    }

    /**
     * Creates a new {@link AcceptedResourceQualifier} instance.
     *
     * @param type  the resource type accepted by the extension.
     */
    AcceptedResourceQualifier(final ResourceType type, final Boolean equals) {
        this.type = Objects.requireNonNull(type, "type must be null");
        this.equals = equals;
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
        return descriptor
                .metadata()
                .attributesForName(METADATA_ATTRIBUTE_NAME)
                .stream()
                .anyMatch(this::matches);
    }

    @SuppressWarnings("unchecked")
    private boolean matches(final ExtensionAttribute attribute) {
        Class<HasMetadata> type = (Class<HasMetadata>) attribute.value("type");
        String kind = (String) attribute.value("kind");
        String apiVersion = (String) attribute.value("apiVersion");
        ResourceType resourceType = null;

        if (type != HasMetadata.class) {
            resourceType = ResourceType.of(type);
        }
        else if (!Strings.isBlank(apiVersion)) {
            resourceType = ResourceType.of(kind, apiVersion);
        }
        else if (!Strings.isBlank(kind)) {
            resourceType = ResourceType.of(kind);
        }

        return equals == Objects.equals(this.type, resourceType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AcceptedResourceQualifier)) return false;
        AcceptedResourceQualifier<?> that = (AcceptedResourceQualifier<?>) o;
        return type.equals(that.type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "@AcceptedResource(apiVersion="
                + type.group()
                + "/"
                + type.apiVersion()
                + ", kind=" + type.kind()
                + ")";
    }
}
