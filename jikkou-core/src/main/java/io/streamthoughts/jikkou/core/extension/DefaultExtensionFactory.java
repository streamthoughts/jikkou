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

import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The default {@link ExtensionFactory} implementation.
 */
public final class DefaultExtensionFactory implements ExtensionFactory {

    private final Configuration configuration;
    private final ExtensionDescriptorRegistry registry;

    /**
     * Creates a new {@link DefaultExtensionFactory} instance.
     *
     * @param registry the {@link ExtensionDescriptorRegistry}.
     */
    public DefaultExtensionFactory(@NotNull final ExtensionDescriptorRegistry registry) {
        this(registry, Configuration.empty());
    }


    /**
     * Creates a new {@link DefaultExtensionFactory} instance.
     *
     * @param configuration the {@link Configuration} that will be passed to {@link Configurable} extension.
     * @param registry      the {@link ExtensionDescriptorRegistry}.
     */
    public DefaultExtensionFactory(@NotNull final ExtensionDescriptorRegistry registry,
                                   @NotNull final Configuration configuration) {
        this.configuration = configuration;
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
        Optional<ExtensionDescriptor<T>> optional = registry.findDescriptorByClass(type, qualifier);
        if (optional.isEmpty())
            throw new NoSuchExtensionException("No extension registered for type '" + type + "'");
        ExtensionDescriptor<T> descriptor = optional.get();
        return registry.getExtensionSupplier(descriptor).get(configuration);
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
        return registry.getExtensionSupplier(descriptor).get(configuration);
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
                .map(this.registry::getExtensionSupplier)
                .map(supplier -> supplier.get(configuration))
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
        return this.registry.findAllDescriptorsByClass(type, qualifier)
                .stream()
                .map(this.registry::getExtensionSupplier)
                .map(supplier -> supplier.get(configuration))
                .toList();
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
