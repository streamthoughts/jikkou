/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.ProviderSelectionContext;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The default {@link ExtensionFactory} implementation.
 */
public final class DefaultExtensionFactory implements ExtensionFactory {

    private final ExtensionDescriptorRegistry registry;

    /**
     * Creates a new {@link DefaultExtensionFactory} instance.
     *
     * @param registry      the {@link ExtensionDescriptorRegistry}.
     */
    public DefaultExtensionFactory(@NotNull final ExtensionDescriptorRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean containsExtension(String type) {
        return this.registry.findDescriptorByAlias(type).isPresent();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> boolean containsExtension(@NotNull String type, Qualifier<T> qualifier) {
        return this.registry.findDescriptorByAlias(type, qualifier).isPresent();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> boolean containsExtension(@NotNull Class<T> type) {
        return this.registry.findDescriptorByClass(type).isPresent();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> boolean containsExtension(@NotNull Class<T> type, Qualifier<T> qualifier) {
        return this.registry.findDescriptorByClass(type, qualifier).isPresent();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> T getExtension(@NotNull Class<T> type) {
        return getExtension(type, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> T getExtension(@NotNull Class<T> type,
                              @Nullable Qualifier<T> qualifier) {
        return getExtension(type, qualifier, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> T getExtension(@NotNull Class<T> type,
                              @Nullable Qualifier<T> qualifier,
                              @Nullable ProviderSelectionContext providerContext) {
        Optional<ExtensionDescriptor<T>> optional = registry.findDescriptorByClass(type, qualifier);
        if (optional.isEmpty()) {
            String error = qualifier != null ?
                    "No extension registered for type '" + type + "', and qualifier '" + qualifier + "'." :
                    "No extension registered for type '" + type + "'.";
            throw new NoSuchExtensionException(error);
        }
        ExtensionDescriptor<T> descriptor = optional.get();
        return registry.getExtensionSupplier(descriptor).get(this, providerContext);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<T> findExtension(@NotNull Class<T> type, @Nullable Qualifier<T> qualifier) {
        return findExtension(type, qualifier, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<T> findExtension(@NotNull Class<T> type,
                                        @Nullable Qualifier<T> qualifier,
                                        @Nullable ProviderSelectionContext providerContext) {
        return registry.findDescriptorByClass(type, qualifier)
                .map(registry::getExtensionSupplier)
                .map(supplier -> supplier.get(this, providerContext));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> T getExtension(@NotNull String type) {
        return getExtension(type, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> T getExtension(@NotNull String type, Qualifier<T> qualifier) {
        Optional<ExtensionDescriptor<T>> optional = registry.findDescriptorByAlias(type, qualifier);
        if (optional.isEmpty())
            throw new NoSuchExtensionException("No extension registered for type '" + type + "'");
        ExtensionDescriptor<T> descriptor = optional.get();
        return registry.getExtensionSupplier(descriptor).get(this, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Collection<T> getAllExtensions(@NotNull String type) {
        return getAllExtensions(type, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Collection<T> getAllExtensions(@NotNull String type,
                                              @Nullable Qualifier<T> qualifier) {
        return this.registry.findAllDescriptorsByAlias(type, qualifier)
                .stream()
                .sorted(Comparator.comparing(ExtensionDescriptor::priority))
                .map(this.registry::getExtensionSupplier)
                .map(supplier -> supplier.get(this, null))
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<T> getAllExtensions(@NotNull Class<T> type) {
        return getAllExtensions(type, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<T> getAllExtensions(@NotNull Class<T> type,
                                        @Nullable Qualifier<T> qualifier) {
        return getAllExtensions(type, qualifier, null);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<T> getAllExtensions(@NotNull Class<T> type,
                                        @Nullable Qualifier<T> qualifier,
                                        @Nullable ProviderSelectionContext providerContext) {
        return this.registry.findAllDescriptorsByClass(type, qualifier)
                .stream()
                .map(this.registry::getExtensionSupplier)
                .map(supplier -> supplier.get(this, providerContext))
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ExtensionDescriptor<?>> getAllDescriptors() {
        return this.registry.getAllDescriptors();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByClass(@NotNull Class<T> type) {
        return this.registry.findAllDescriptorsByClass(type);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ExtensionDescriptor<?>> findAllDescriptors(@NotNull Qualifier<?> qualifier) {
        return this.registry.findAllDescriptors(qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByClass(@NotNull Class<T> type, @Nullable Qualifier<T> qualifier) {
        return this.registry.findAllDescriptorsByClass(type, qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByAlias(@NotNull String alias) {
        return this.registry.findAllDescriptorsByAlias(alias);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> List<ExtensionDescriptor<T>> findAllDescriptorsByAlias(@NotNull String alias, @Nullable Qualifier<T> qualifier) {
        return this.registry.findAllDescriptorsByAlias(alias, qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByAlias(@NotNull String alias) {
        return this.registry.findDescriptorByAlias(alias);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByAlias(@NotNull String alias, @Nullable Qualifier<T> qualifier) {
        return this.registry.findDescriptorByAlias(alias, qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByClass(@NotNull Class<T> type) {
        return this.registry.findDescriptorByClass(type);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> Optional<ExtensionDescriptor<T>> findDescriptorByClass(@NotNull Class<T> type, @Nullable Qualifier<T> qualifier) {
        return this.registry.findDescriptorByClass(type, qualifier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> ExtensionSupplier<T> getExtensionSupplier(ExtensionDescriptor<T> descriptor) {
        return this.registry.getExtensionSupplier(descriptor);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void registerDescriptor(@NotNull ExtensionDescriptor<T> descriptor) {
        this.registry.registerDescriptor(descriptor);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ExtensionFactory duplicate() {
        return new DefaultExtensionFactory(this.registry.duplicate());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier) {
        registry.register(type, supplier);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public <T> void register(@NotNull Class<T> type,
                             @NotNull Supplier<T> supplier,
                             ExtensionDescriptorModifier... modifiers) {
        registry.register(type, supplier, modifiers);
    }
}
