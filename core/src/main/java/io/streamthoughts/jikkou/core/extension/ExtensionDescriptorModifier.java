/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

/**
 * Callback for modifying a given extension descriptor.
 */
@FunctionalInterface
public interface ExtensionDescriptorModifier {

    /**
     * Modifies the given {@link ExtensionDescriptor}.
     *
     * @param descriptor    the {@link ExtensionDescriptor} instance.
     * @param <T>           the extension type.
     * @return              the modified descriptor or a new instance.
     */
    <T> ExtensionDescriptor<T> apply(final ExtensionDescriptor<T> descriptor);
}
