/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import java.util.ArrayList;
import java.util.List;
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
    private final String title;
    private final List<Example> examples;
    private final ExtensionCategory category;
    private final List<ConfigPropertySpec> properties;
    private final String provider;
    private final Class<T> type;
    private final Supplier<T> supplier;
    private final boolean isEnabled;
    private final Set<String> aliases;
    private final ClassLoader classLoader;
    private ExtensionMetadata metadata;

    /**
     * Creates a new {@link DefaultExtensionDescriptor} instance.
     *
     * @param name        The name of the extension.
     * @param description The description of the extension.
     * @param category    The category of the extension.
     * @param provider    The provider of the extension.
     * @param type        The type of the extension.
     * @param classLoader The extension ClassLoader.
     * @param supplier    The Supplier of the extension.
     */
    public DefaultExtensionDescriptor(final String name,
                                      final String title,
                                      final String description,
                                      final List<Example> examples,
                                      final ExtensionCategory category,
                                      final List<ConfigPropertySpec> properties,
                                      final String provider,
                                      final Class<T> type,
                                      final ClassLoader classLoader,
                                      final Supplier<T> supplier,
                                      final boolean isEnabled) {
        Objects.requireNonNull(type, "type can't be null");
        Objects.requireNonNull(supplier, "supplier can't be null");
        this.name = name;
        this.description = description;
        this.title = title;
        this.examples = examples == null ? null : new ArrayList<>(examples);
        this.properties = properties == null ? null : new ArrayList<>(properties);
        this.category = category;
        this.provider = provider;
        this.supplier = supplier;
        this.type = type;
        this.classLoader = classLoader == null ? type.getClassLoader() : classLoader;
        this.aliases = new TreeSet<>();
        this.metadata = new ExtensionMetadata();
        this.isEnabled = isEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    @Override
    public String title() {
        return title;
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
    public List<Example> examples() {
        return examples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigPropertySpec> properties() {
        return properties;
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
    public ExtensionCategory category() {
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String provider() {
        return provider;
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
