/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core;

import io.streamthoughts.jikkou.core.config.ConfigProperty;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public abstract class BaseApiConfigurator implements ApiConfigurator {

    private final ExtensionDescriptorRegistry registry;

    private Configuration configuration;

    /**
     * Creates a new {@link BaseApiConfigurator} instance.
     *
     * @param registry  the ExtensionDescriptorRegistry.
     */
    public BaseApiConfigurator(final @NotNull ExtensionDescriptorRegistry registry) {
        this.registry = Objects.requireNonNull(
                registry,
                "registry must not be null"
        );
    }

    protected Configuration configuration() {
        return configuration;
    }

    protected <T> T getPropertyValue(final ConfigProperty<T> property) {
        return property.get(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    public <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(
            @NotNull final B builder,
            @NotNull final Configuration configuration) {
        this.configuration = configuration;
        return configure(builder);
    }

    protected abstract <A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> B configure(B builder);

    public <T extends Extension> Optional<ExtensionDescriptor<T>> findExtensionDescriptor(final String alias) {
        return registry.findDescriptorByAlias(alias);
    }
}
