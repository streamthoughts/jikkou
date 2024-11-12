/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.config;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specification of a configuration property.
 *
 * @param name          The name.
 * @param type          The type.
 * @param description   The description.
 * @param defaultValue  The default value.
 * @param required      Specify whether the property is required.
 */
public record ConfigPropertySpec(@NotNull String name,
                                 @NotNull Class<?> type,
                                 @Nullable String description,
                                 @Nullable Object defaultValue,
                                 boolean required) {

    public ConfigPropertySpec {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
    }
}
