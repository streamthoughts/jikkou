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
     * @return the extension name.
     */
    String name();

    /**
     * Returns the configuration associated with this extension.
     * <p>
     * If the extension was registered directly, this method returns the configuration
     * it was registered with. If the extension was registered through a provider,
     * the returned configuration may be empty.
     *
     * @return the extension configuration
     */
    Configuration configuration();

    /**
     * Gets a new extension context from the specified extension type.
     *
     * @param extension The extension type.
     * @return the {@link ExtensionContext}.
     */
    ExtensionContext contextForExtension(Class<? extends Extension> extension);

    /**
     * Gets the {@link ExtensionProvider} attached to this context.
     *
     * @param <T> Type of extension provider.
     * @throws NoSuchElementException if no provider is attached to this context.
     */
    <T extends ExtensionProvider> T provider();

    /**
     * Static helper method for constructing a new {@link ExtensionContext} from a given configuration.
     * <p>
     * This method is expected to be used only for testing purpose.
     *
     * @param configuration the configuration.
     * @return a new {@link ExtensionContext}
     */
    static ExtensionContext fromConfiguration(Configuration configuration) {
        return new ExtensionContext() {
            @Override
            public String name() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Configuration configuration() {
                return configuration;
            }

            @Override
            public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends ExtensionProvider> T provider() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
