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

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * The {@link DefaultExtensionDescriptor} is the base class used from describing extension.
 *
 * @param <T> the component-type.
 */
public class DefaultExtensionDescriptor<T> implements ExtensionDescriptor<T> {

    private final String name;
    private final String description;
    private final String category;
    private final Class<T> type;
    private final Supplier<T> supplier;
    private final boolean isEnabled;
    private final Set<String> aliases;
    private final ClassLoader classLoader;
    private ExtensionMetadata metadata;

    /**
     * Creates a new {@link DefaultExtensionDescriptor} instance.
     *
     * @param name        the name of the extension.
     * @param description the description of the extension.
     * @param description the category of the extension.
     * @param type        the type of the extension.
     * @param classLoader the extension classloader.
     * @param supplier    the supplier of the extension.
     */
    public DefaultExtensionDescriptor(final String name,
                                      final String description,
                                      final String category,
                                      final Class<T> type,
                                      final ClassLoader classLoader,
                                      final Supplier<T> supplier,
                                      final boolean isEnabled) {
        Objects.requireNonNull(type, "type can't be null");
        Objects.requireNonNull(supplier, "supplier can't be null");
        this.name = name;
        this.description = description;
        this.category = category;
        this.supplier = supplier;
        this.type = type;
        this.classLoader = classLoader == null ? type.getClassLoader() : classLoader;
        this.aliases = new TreeSet<>();
        this.metadata = new ExtensionMetadata();
        this.isEnabled = isEnabled;
    }

    /**
     * Creates a new {@link DefaultExtensionDescriptor} instance from the given one.
     *
     * @param descriptor the {@link DefaultExtensionDescriptor} to copy.
     */
    protected DefaultExtensionDescriptor(final ExtensionDescriptor<T> descriptor) {
        this(
                descriptor.name(),
                descriptor.description(),
                descriptor.category(),
                descriptor.type(),
                descriptor.classLoader(),
                descriptor.supplier(),
                descriptor.isEnabled()
        );
        metadata = descriptor.metadata();
        aliases.addAll(descriptor.aliases());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionMetadata metadata() {
        return metadata;
    }

    public void metadata(final ExtensionMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String category() {
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAliases(final Set<String> aliases) {
        this.aliases.addAll(aliases);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> aliases() {
        return aliases;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String className() {
        return type.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<T> supplier() {
        return supplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> type() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultExtensionDescriptor)) return false;
        DefaultExtensionDescriptor<?> that = (DefaultExtensionDescriptor<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(supplier, that.supplier);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, type, supplier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" +
                "name=" + name +
                ", description=" + description +
                ", type=" + type +
                ", aliases=" + aliases +
                ", metadata=" + metadata +
                ']';
    }
}
