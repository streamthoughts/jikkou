/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.spi.ExtensionProvider;

public class ExtensionContextDecorator implements ExtensionContext {

    private final ExtensionContext delegate;

    /**
     * Creates a new {@link ExtensionContextDecorator} instance.
     * @param context   The ExtensionContext.
     */
    public ExtensionContextDecorator(ExtensionContext context) {
        this.delegate = context;
    }

    /** {@inheritDoc} **/
    @Override
    public String name() {
        return delegate.name();
    }

    /** {@inheritDoc} **/
    @Override
    public Configuration configuration() {
        return delegate.configuration();
    }

    /** {@inheritDoc}
    @Override
    public Map<String, ConfigProperty> configProperties() {
        return delegate.configProperties();
    }**/

    /** {@inheritDoc}
    @Override
    public <T> ConfigProperty<T> configProperty(String key) {
        return delegate.configProperty(key);
    }
     **/

    /** {@inheritDoc} **/
    @Override
    public ExtensionContext contextForExtension(Class<? extends Extension> extension) {
        return delegate.contextForExtension(extension);
    }
    /** {@inheritDoc} **/
    @Override
    public <T extends ExtensionProvider> T provider() {
        return delegate.provider();
    }
}
