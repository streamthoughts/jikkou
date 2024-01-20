/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                                 @Nullable String defaultValue,
                                 boolean required) {

    public static final String NULL_VALUE = "_NULL_";
    public static final String NO_DEFAULT_VALUE = "__no_default_value__";


    public ConfigPropertySpec {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
    }
}
