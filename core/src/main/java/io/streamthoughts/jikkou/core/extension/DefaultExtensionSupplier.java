/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;


import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionCreationException;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultExtensionSupplier<T> implements ExtensionSupplier<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionSupplier.class);

    private final ExtensionDescriptorRegistry registry;
    private final ExtensionDescriptor<T> descriptor;

    /**
     * Creates a new {@link DefaultExtensionSupplier} instance.
     *
     * @param descriptor the descriptor of the extension. Cannot be {@code null}.
     */
    public DefaultExtensionSupplier(final @NotNull ExtensionDescriptorRegistry registry,
                                    final @NotNull ExtensionDescriptor<T> descriptor) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor cannot be null");
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public T get(Configuration configuration) {
        Supplier<T> supplier = descriptor.supplier();
        try {
            T t = supplier.get();
            if (t == null) {
                throw new ExtensionCreationException(String.format(
                        "Supplier for extension type '%s' returned null object"
                        , descriptor.className()));
            }
            if (t instanceof Extension extension) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Initializing extension '{}'", extension.getName());
                }
                extension.init(new DefaultExtensionContext(registry, descriptor, configuration));
            }
            return t;
        } catch (Exception e) {
            LOG.error("Failed to get extension instance for type: {} (name: {}, provider: {}).",
                    descriptor.className(),
                    descriptor.name(),
                    descriptor.provider(),
                    e
            );
            throw new ExtensionCreationException(e);
        }
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ExtensionDescriptor<T> descriptor() {
        return descriptor;
    }
}
