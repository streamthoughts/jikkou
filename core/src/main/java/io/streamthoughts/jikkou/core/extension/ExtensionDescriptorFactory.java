/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import io.streamthoughts.jikkou.core.extension.exceptions.ExtensionRegistrationException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * Factory to create new {@link ExtensionDescriptorFactory} instance.
 */
public interface ExtensionDescriptorFactory {

    /**
     * Makes a new {@link ExtensionDescriptorFactory} instance.
     *
     * @param extensionType     the type of the extension. Cannot be {@code null}.
     * @param extensionSupplier the supplier of the extension. Cannot be {@code null}.
     * @return a new instance of {@link ExtensionDescriptor}.
     * @throws ExtensionRegistrationException if an exception occurred while building the descriptor.
     */
    <T> ExtensionDescriptor<T> make(@NotNull final Class<T> extensionType,
                                    @NotNull final Supplier<T> extensionSupplier
    );
}