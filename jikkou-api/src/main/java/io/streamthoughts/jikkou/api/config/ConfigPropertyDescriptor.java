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
package io.streamthoughts.jikkou.api.config;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfigPropertyDescriptor {

    private final String name;
    private final String description;
    private final boolean isRequired;
    private final String defaultValue;

    private final Class<?> type;

    public ConfigPropertyDescriptor(@NotNull final String name,
                                    @NotNull final Class<?> type,
                                    @Nullable final String description,
                                    @Nullable final String defaultValue,
                                    final boolean isRequired) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.description = description;
        this.isRequired = isRequired;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public String description() {
        return description;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String defaultValue() {
        return defaultValue;
    }
}
