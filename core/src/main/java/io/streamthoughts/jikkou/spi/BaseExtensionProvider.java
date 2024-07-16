/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.spi;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.exceptions.ConfigException;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for implementing the {@link ExtensionProvider} interface.
 */
public abstract class BaseExtensionProvider implements ExtensionProvider {

    protected Configuration configuration;

    /** {@inheritDoc} **/
    @Override
    public void configure(@NotNull Configuration configuration) throws ConfigException {
       this.configuration = configuration;
    }
}
