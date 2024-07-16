/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.runtime.configurator;

import io.streamthoughts.jikkou.core.BaseApiConfigurator;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifiers;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExtensionApiConfigurator<E extends Extension> extends BaseApiConfigurator {

    private static final Logger LOG = LoggerFactory.getLogger(ExtensionApiConfigurator.class);

    private final ConfigProperty<List<ExtensionConfigEntry>> property;

    /**
     * Creates a new {@link BaseApiConfigurator} instance.
     *
     * @param registry the {@link ExtensionDescriptorRegistry}.
     * @param property the {@link ConfigProperty}.
     */
    public ExtensionApiConfigurator(@NotNull final ExtensionDescriptorRegistry registry,
                                    @NotNull final ConfigProperty<List<ExtensionConfigEntry>> property) {
        super(registry);
        this.property = property;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    protected <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder) {
        LOG.info("Loading all extensions from configuration property: '{}'", property.key());
        List<ExtensionConfigEntry> extensions = getPropertyValue(property);

        for (ExtensionConfigEntry extension : extensions) {
            String extensionClass = extension.type();
            Optional<ExtensionDescriptor<E>> optional = findExtensionDescriptor(extensionClass);
            if (optional.isEmpty()) {
                throw new JikkouRuntimeException(
                        "Extension not found for class or alias : '" + extensionClass + "'.");
            }
            ExtensionDescriptor<E> descriptor = optional.get();
            builder = builder.register(
                descriptor.type(),
                descriptor.supplier(),
                ExtensionDescriptorModifiers.enabled(true),
                ExtensionDescriptorModifiers.withName(extension.name()),
                ExtensionDescriptorModifiers.withConfiguration(extension.config()),
                ExtensionDescriptorModifiers.withProvider(descriptor.provider(), descriptor.providerSupplier()),
                ExtensionDescriptorModifiers.withPriority(descriptor.priority())
            );
            LOG.info(
                    "Registered extension for type {} (name={}, priority={}):\n\t{}",
                    extension.type(),
                    extension.name(),
                    extension.priority(),
                    extension.config().toPrettyString("\n\t"));
        }
        return builder;
    }
}
