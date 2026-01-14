/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.ProviderSelectionContext;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/**
 * Default {@link ExtensionContext}.
 */
public final class DefaultExtensionContext implements ExtensionContext {

    private final ExtensionFactory factory;
    private final ExtensionDescriptor<?> descriptor;
    private final ProviderSelectionContext providerContext;

    /**
     * Creates a new {@link DefaultExtensionContext} instance.
     *
     * @param factory         The ExtensionFactory
     * @param descriptor      The ExtensionDescriptor
     * @param providerContext The ProviderSelectionContext (optional)
     */
    public DefaultExtensionContext(final ExtensionFactory factory,
                                   final ExtensionDescriptor<?> descriptor,
                                   @Nullable final ProviderSelectionContext providerContext) {
        this.factory = factory;
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor cannot be null");
        this.providerContext = providerContext;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String name() {
        return descriptor.name();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Configuration configuration() {
        return descriptor.configuration();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
        if (factory == null) throw new IllegalStateException("No factory configured");
        return factory.findDescriptorByClass(extension)
            .map(descriptor -> new DefaultExtensionContext(factory, descriptor, providerContext))
            .orElseThrow(() -> new NoSuchExtensionException("No extension registered for type: " + extension.getName()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ExtensionProvider> T provider() {
        return (T) Optional.ofNullable(descriptor.providerSupplier())
            .map(supplier -> supplier.get(providerContext))
            .orElseThrow(() -> new NoSuchElementException("No provider registered for extension: " + descriptor.name()));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return String.format("ExtensionContext[name=%s, type=%s]", name(), descriptor.className());
    }
}
