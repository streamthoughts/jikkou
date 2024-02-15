/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract implementation of an {@link Extension} that manages the {@link ExtensionContext} instance.
 */
public abstract class ContextualExtension implements Extension {

    private ExtensionContext context;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void init(@NotNull ExtensionContext context) {
        this.context = context;
    }

    /**
     * Get the extension's context set during initialization.
     *
     * @return The ExtensionContext.
     */
    public ExtensionContext extensionContext() {
        return Optional
                .ofNullable(context)
                .orElseThrow(() -> new IllegalStateException("Not initialized"));
    }
}
