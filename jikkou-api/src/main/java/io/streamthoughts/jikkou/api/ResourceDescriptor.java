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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.annotation.Description;
import io.streamthoughts.jikkou.annotation.Names;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.common.utils.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Metadata information about a {@link io.streamthoughts.jikkou.api.model.Resource} object.
 */
public final class ResourceDescriptor {

    public static final String DEFAULT_DESCRIPTION = "";
    private final ResourceType type;
    private final String description;
    private final Class<? extends HasMetadata> clazz;
    private String singularName;
    private String pluralName;
    private Set<String> shortNames;

    /**
     * Creates a new {@link ResourceDescriptor} instance.
     *
     * @param clazz the resource class.
     */
    public ResourceDescriptor(Class<? extends HasMetadata> clazz) {
        this(ResourceType.create(clazz), clazz);
    }

    /**
     * Creates a new {@link ResourceDescriptor} instance.
     *
     * @param type the resource type.
     * @param clazz the resource class.
     */
    public ResourceDescriptor(ResourceType type, Class<? extends HasMetadata> clazz) {
        this.type = type;
        this.clazz = clazz;
        this.description = Optional.ofNullable(clazz.getAnnotation(Description.class))
                .map(Description::value)
                .orElse(DEFAULT_DESCRIPTION);

        Names names = clazz.getAnnotation(Names.class);
        if (names != null) {
            this.singularName =  Strings.isBlank(names.singular()) ? null : names.singular();
            this.pluralName = Strings.isBlank(names.plural()) ? null : names.plural();
            this.shortNames = new TreeSet<>(Arrays.asList(names.shortNames()));
        } else {
            this.singularName = null;
            this.pluralName = null;
            this.shortNames = Collections.emptySet();
        }
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
