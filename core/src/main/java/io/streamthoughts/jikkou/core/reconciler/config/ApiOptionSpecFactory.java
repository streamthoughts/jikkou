/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.reconciler.config;

import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class to create new {@link ApiResourceVerbOptionList}.
 */
public final class ApiOptionSpecFactory {

    /**
     * Creates a new {@link ApiResourceVerbOptionList} for the specified extension class.
     *
     * @param descriptor the extension descriptor.
     * @return a new {@link ApiResourceVerbOptionList} object.
     */
    public List<ApiOptionSpec> make(@NotNull ExtensionDescriptor<?> descriptor) {
        Objects.requireNonNull(descriptor, "descriptor cannot be null");
        return descriptor.properties()
                .stream()
                .map(config -> new ApiOptionSpec(
                        config.name(),
                        config.description(),
                        config.type(),
                        config.defaultValue(),
                        config.required())
                ).toList();
    }
}
