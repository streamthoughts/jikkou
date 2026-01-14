/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.ProviderSelectionContext;
import io.streamthoughts.jikkou.spi.ExtensionProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for supplying extension instance.
 */
public interface ProviderSupplier {

    /**
     * Gets a configured ExtensionProvider instance, optionally using a ProviderSelectionContext
     * to select a specific provider configuration.
     *
     * @param providerContext the provider selection context (optional)
     * @return a configured ExtensionProvider instance
     */
    ExtensionProvider get(@Nullable ProviderSelectionContext providerContext);
}
