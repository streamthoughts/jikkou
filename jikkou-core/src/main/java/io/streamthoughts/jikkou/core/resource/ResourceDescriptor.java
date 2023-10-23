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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Metadata information about a {@link Resource} object.
 */
public final class ResourceDescriptor {

    private final ResourceType type;
    private final String description;
    private final Class<? extends HasMetadata> clazz;
    private String singularName;
    private String pluralName;
    private Set<String> shortNames;

    /**
     * Creates a new {@link ResourceDescriptor} instance.
     * @param type              the type of the resource.
     * @param description       the description of the resource.
     * @param resourceClass     the class of the resource.
     */
    public ResourceDescriptor(@NotNull ResourceType type,
                              @NotNull String description,
                              @NotNull Class<? extends HasMetadata> resourceClass) {
        this(type, description, resourceClass, null, null, Collections.emptySet());
    }

    /**
     * Creates a new {@link ResourceDescriptor} instance.
     * @param type              the type of the resource.
     * @param description       the description of the resource.
     * @param resourceClass     the class of the resource.
     * @param singularName      the singular name of the resource.
     * @param pluralName        the plural name of the resource.
     * @param shortNames        the short name of the resource.
     */
    public ResourceDescriptor(@NotNull ResourceType type,
                              @NotNull String description,
                              @NotNull Class<? extends HasMetadata> resourceClass,
                              @Nullable String singularName,
                              @Nullable String pluralName,
                              @NotNull Set<String> shortNames) {
        this.type = type;
        this.description = description;
        this.clazz = resourceClass;
        this.singularName = singularName;
        this.pluralName = pluralName;
        this.shortNames = shortNames;
    }

    /**
     * Gets the type of the described resource.
     *
     * @return  the resource type.
     */
    public ResourceType resourceType() {
        return type;
    }

    /**
     * Gets the singular name of the described resource.
     *
     * @return  the singular name.
     */
    public String singularName() {
        return Optional
                .ofNullable(this.singularName)
                .orElse(type.getKind().toLowerCase(Locale.ROOT));
    }


    /**
     * Sets the singular name of the described resource.
     *
     * @param singularName the singular name.
     * @return this object so methods can be chained together; never null
     */
    public ResourceDescriptor setSingularName(final String singularName) {
        this.singularName = singularName;
        return this;
    }

    /**
     * Gets the plural name of the described resource.
     *
     * @return  the plural name.
     */
    public Optional<String> pluralName() {
        return Optional.ofNullable(this.pluralName);
    }

    /**
     * Sets the plural name of the described resource.
     *
     * @param pluralName the plural name.
     * @return this object so methods can be chained together; never null
     */
    public ResourceDescriptor setPluralName(final String pluralName) {
        this.pluralName = pluralName;
        return this;
    }

    /**
     * Gets the short names of the described resource.
     *
     * @return  the short names.
     */
    public Set<String> shortNames() {
        return Optional.ofNullable(this.shortNames).orElse(Collections.emptySet());
    }

    /**
     * Sets the short names of the described resource.
     *
     * @param shortNames the short names.
     * @return this object so methods can be chained together; never null
     */
    public ResourceDescriptor setShortNames(Set<String> shortNames) {
        this.shortNames = shortNames;
        return this;
    }

    /**
     * Gets the class representing the described resource.
     *
     * @return  the resource class.
     */
    public Class<? extends HasMetadata> resourceClass() {
        return clazz;
    }

    /**
     * Gets the kind of the described resource.
     *
     * @return  the resource kind.
     */
    public String kind() {
        return type.getKind();
    }

    /**
     * Gets the api version of the described resource.
     *
     * @return  the api version.
     */
    public String apiVersion() {
        return type.getApiVersion();
    }

    /**
     * Gets the group of the described resource.
     *
     * @return  the resource group.
     */
    public String group() {
        return type.getGroup();
    }

    /**
     * Gets the description of the described resource.
     *
     * @return  the resource description.
     */
    public String description() {
        return this.description;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceDescriptor that = (ResourceDescriptor) o;
        return Objects.equals(type, that.type);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
