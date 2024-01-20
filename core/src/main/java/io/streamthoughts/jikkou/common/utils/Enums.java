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
package io.streamthoughts.jikkou.common.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility method for manipulating enumSpec.
 */
public final class Enums {

    /**
     * Gets the enum for specified string name.
     *
     * @param value    The enum raw value.
     * @param enumType The enum class type.
     * @param <T>      The enum type.
     * @return The Enum.
     * @throws IllegalArgumentException if no enum exists for the specified value.
     */
    public static <T extends Enum<T>> T getForNameIgnoreCase(final @Nullable String value,
                                                             final @NotNull Class<T> enumType) {
        if (value == null) throw new IllegalArgumentException("Unsupported value 'null'");

        T[] values = enumType.getEnumConstants();
        return Arrays.stream(values)
                .filter(e -> e.name().equals(value.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "Unsupported enum value '%s'. Expected one of: %s",
                        value,
                        Arrays.stream(values)
                                .map(Enum::name)
                                .collect(Collectors.joining(", ", "[", "]"))
                )));
    }

    /**
     * Gets the set of enums for specified raw values.
     *
     * @param values   The enum raw values as a comma separated list.
     * @param enumType The enum class type.
     * @param <T>      The enum type.
     * @return The Enum.
     * @throws IllegalArgumentException if no enum exists for the specified value.
     */
    public static <T extends Enum<T>> Set<T> getForNamesIgnoreCase(final @Nullable String values,
                                                                   final @NotNull Class<T> enumType) {
        if (values == null) throw new IllegalArgumentException("Unsupported values 'null'");
        Set<String> set = Arrays.stream(values.split(",")).map(String::trim).collect(Collectors.toSet());
        return getForNamesIgnoreCase(set, enumType);
    }

    /**
     * Gets the set of enums for specified raw values.
     *
     * @param values   The enum raw values.
     * @param enumType The enum class type.
     * @param <T>      The enum type.
     * @return The Enum.
     * @throws IllegalArgumentException if no enum exists for the specified value.
     */
    public static <T extends Enum<T>> Set<T> getForNamesIgnoreCase(final @Nullable Set<String> values,
                                                                   final @NotNull Class<T> enumType) {
        if (values == null) throw new IllegalArgumentException("Unsupported values 'null'");
        return values.stream().map(value -> getForNameIgnoreCase(value, enumType)).collect(Collectors.toSet());
    }

    private Enums() {
    }
}
