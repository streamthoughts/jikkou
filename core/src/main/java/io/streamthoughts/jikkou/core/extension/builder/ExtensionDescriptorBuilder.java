/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.builder;

import io.streamthoughts.jikkou.core.config.ConfigPropertySpec;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.Example;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionMetadata;
import io.streamthoughts.jikkou.core.extension.ProviderSupplier;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Builder for creating new {@link ExtensionDescriptor} instances.
 *
 * @param <T> type of the extension.
 */
public final class ExtensionDescriptorBuilder<T> implements ExtensionDescriptor<T> {

    private String name;
    private String title;
    private String description;
    private List<Example> examples;
    private List<ConfigPropertySpec> properties;
    private ExtensionCategory category;
    private Class<? extends ExtensionProvider> provider;
    private ProviderSupplier providerSupplier;
    private ExtensionMetadata metadata;
    private Class<T> type;
    private boolean isEnabled;
    private ClassLoader classLoader;
    private Supplier<T> supplier;
    private final Set<String> aliases = new HashSet<>();
    private Configuration configuration;
    private Integer priority;

    /**
     * Static helper method for creating a new {@link ExtensionDescriptorBuilder} instance.
     *
     * @param <T> the Extension type.
     * @return the new {@link ExtensionDescriptorBuilder} instance.
     */
    public static <T> ExtensionDescriptorBuilder<T> builder() {
        return new ExtensionDescriptorBuilder<>();
    }

    /**
     * Static helper method for creating a new {@link ExtensionDescriptorBuilder} instance.
     *
     * @param descriptor the {@link ExtensionDescriptor} instance.
     * @param <T>        the Extension type.
     * @return the new {@link ExtensionDescriptorBuilder} instance.
     */
    public static <T> ExtensionDescriptorBuilder<T> builder(final ExtensionDescriptor<T> descriptor) {
        return new ExtensionDescriptorBuilder<T>()
            .name(descriptor.name())
            .title(descriptor.title())
            .description(descriptor.description())
            .examples(descriptor.examples())
            .type(descriptor.type())
            .category(descriptor.category())
            .properties(descriptor.properties())
            .provider(descriptor.provider(), descriptor.providerSupplier())
            .metadata(descriptor.metadata())
            .classLoader(descriptor.classLoader())
            .supplier(descriptor.supplier())
            .configuration(descriptor.configuration())
            .isEnabled(descriptor.isEnabled());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Sets the name of the Extension.
     *
     * @param name the name of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String title() {
        return title;
    }

    public ExtensionDescriptorBuilder<T> title(String title) {
        this.title = title;
        return this;
    }

    public ExtensionDescriptorBuilder<T> examples(List<Example> examples) {
        this.examples = examples;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Example> examples() {
        return examples;
    }

    public ExtensionDescriptorBuilder<T> properties(List<ConfigPropertySpec> properties) {
        this.properties = properties;
        return this;
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
    public Integer priority() {
        return priority;
    }

    public ExtensionDescriptorBuilder<T> priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return name;
    }

    /**
     * Sets the description of the Extension.
     *
     * @param description the description of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> description(final String description) {
        this.description = description;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionMetadata metadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the Extension.
     *
     * @param metadata the metadata of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> metadata(final ExtensionMetadata metadata) {
        this.metadata = metadata;
        return this;
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
     * Specifies if the extension is enabled.
     *
     * @param isEnabled specifies if the extension is enabled.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> isEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionCategory category() {
        return category;
    }

    public ExtensionDescriptorBuilder<T> category(ExtensionCategory category) {
        this.category = category;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<? extends ExtensionProvider> provider() {
        return provider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProviderSupplier providerSupplier() {
        return providerSupplier;
    }

    public ExtensionDescriptorBuilder<T> provider(Class<? extends ExtensionProvider> provider,
                                                  ProviderSupplier supplier) {
        this.provider = provider;
        this.providerSupplier = supplier;
        return this;
    }

    /**
     * Sets the classLoader of the Extension.
     *
     * @param classLoader the classLoader of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> classLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
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
    public Supplier<T> supplier() {
        return supplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Sets the configuration of the Extension.
     *
     * @param configuration the configuration of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> configuration(final Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the supplier of the Extension.
     *
     * @param supplier the supplier of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> supplier(final Supplier<T> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<T> type() {
        return type;
    }

    /**
     * Sets the type of the Extension.
     *
     * @param type the type of the Extension.
     * @return {@code this}
     */
    public ExtensionDescriptorBuilder<T> type(final Class<T> type) {
        this.type = type;
        return this;
    }

    public ExtensionDescriptor<T> build() {
        DefaultExtensionDescriptor<T> descriptor = new DefaultExtensionDescriptor<>(
            name,
            title,
            description,
            examples,
            category,
            properties,
            provider,
            providerSupplier,
            type,
            classLoader,
            supplier,
            configuration,
            isEnabled,
            priority
        );
        descriptor.metadata(metadata);
        descriptor.addAliases(aliases);
        return descriptor;
    }
}
