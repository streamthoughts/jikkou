/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.common.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility method for manipulating enumSpec.
 */
public final class Enums {

    public static <E extends Enum<E>> E safeValueOf(Class<E> type, String value) {
        try {
            return Strings.isNullOrEmpty(value) ? null : Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

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
                                                             final @NotNull Class<T> enumType,
                                                             final T defaultValue) {
        if (value == null) throw new IllegalArgumentException("Unsupported value 'null'");

        T[] values = enumType.getEnumConstants();
        return Arrays.stream(values)
            .filter(e -> e.name().equals(value.toUpperCase(Locale.ROOT)))
            .findFirst()
            .orElse(defaultValue);
    }

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
        return values.stream()
            .filter(Predicate.not(String::isBlank))
            .map(value -> getForNameIgnoreCase(value, enumType))
            .collect(Collectors.toSet());
    }

    private Enums() {
    }
}
