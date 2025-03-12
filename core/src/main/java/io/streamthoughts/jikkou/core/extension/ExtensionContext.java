/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import java.util.NoSuchElementException;

/**
 * Extension context.
 *
 * @see Extension#init(ExtensionContext).
 */
public interface ExtensionContext {

    /**
     * Returns the name of the extension.
     *
     * @return The name.
     */
    String name();

    /**
     * Returns the provider configuration.
     *
     * @return The configuration.
     */
    Configuration configuration();

    /**
     * Gets a new extension context from the specified extension type.
     *
     * @param extension The extension type.
     * @return The ExtensionContext.
     */
    ExtensionContext contextForExtension(Class<? extends Extension> extension);

    /**
     * Gets the {@link ExtensionProvider} attached to this context.
     *
     * @param <T> Type of extension provider.
     * @throws NoSuchElementException if no provider is attached to this context.
     */
    <T extends ExtensionProvider> T provider();
}
