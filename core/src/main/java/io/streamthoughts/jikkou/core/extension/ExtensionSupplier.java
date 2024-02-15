/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionCreationException;

/**
 *
 * @param <T> the type of the extension returned from this supplier.
 */
public interface ExtensionSupplier<T> {

    /**
     * Create a new extension instance.
     *
     * @param configuration the {@link Configuration} that can be used to configure the extension.
     * @return a new instance of {@link T}.
     *
     * @throws ExtensionCreationException if the extension cannot be created or configured.
     */
    T get(Configuration configuration);

    /**
     * Gets the descriptor for the extension supplied by this class.
     *
     * @return  the {@link ExtensionDescriptor}.
     */
    ExtensionDescriptor<T> descriptor();
}
