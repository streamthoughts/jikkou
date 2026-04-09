/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core;

import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.extension.Extension;
import io.jikkou.core.extension.ExtensionDescriptor;
import io.jikkou.core.extension.ExtensionDescriptorRegistry;
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
    public BaseApiConfigurator(final ExtensionDescriptorRegistry registry) {
        this.registry = registry;
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
        return Optional.ofNullable(this.registry).flatMap(registry -> registry.findDescriptorByAlias(alias));
    }
}
