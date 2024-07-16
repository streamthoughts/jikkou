/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.spi;

import io.streamthoughts.jikkou.core.annotation.Provider;
import io.streamthoughts.jikkou.core.config.Configurable;
import io.streamthoughts.jikkou.core.extension.ExtensionRegistry;
import io.streamthoughts.jikkou.core.models.HasName;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 * Service interface for registering extensions and resources to Jikkou at runtime.
 * The implementations are discovered using the standard Java {@link java.util.ServiceLoader} mechanism.
 *
 * Hence, the fully qualified name of the extension classes that implement the {@link ExtensionProvider}
 * interface must be added to a {@code META-INF/services/io.streamthoughts.jikkou.spi.ExtensionProvider} file.
 * </pre>
 */
public interface ExtensionProvider extends HasName, Configurable {

    /**
     * Registers the extensions for this provider.
     *
     * @param registry The ExtensionRegistry.
     */
    void registerExtensions(@NotNull ExtensionRegistry registry);

    /**
     * Registers the resources for this provider.
     *
     * @param registry The ResourceRegistry.
     */
    void registerResources(@NotNull ResourceRegistry registry);

    /** {@inheritDoc} **/
    @Override
    default String getName() {
        Class<? extends ExtensionProvider> thatClass = this.getClass();
        return Optional.ofNullable(thatClass.getAnnotation(Provider.class))
            .map(Provider::name)
            .orElseGet(HasName.super::getName);
    }
}
