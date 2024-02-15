/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension;

import java.util.List;
import java.util.Set;

/**
 * Default interface to generate aliases for an extension.
 */
public interface ExtensionAliasesGenerator {

    /**
     * Gets unique aliases for the specified {@link ExtensionDescriptor} descriptor.
     *
     * @param descriptor        the {@link ExtensionDescriptor} descriptor.
     * @param allDescriptors    the list of existing {@link ExtensionDescriptor} instances.
     *
     * @return                  the set of unique aliases.
     */
    Set<String> getAliasesFor(final ExtensionDescriptor<?> descriptor,
                              final List<ExtensionDescriptor<?>> allDescriptors);
}
