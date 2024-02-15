/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ApiOptionSpec.
 *
 * @param name         the name of the parameter.
 * @param description  the description of the parameter.
 * @param type         the type of the parameter.
 * @param defaultValue the default value of the parameter.
 * @param required     specifies if the parameter is required.
 */
@JsonPropertyOrder({
        "name",
        "description",
        "type",
        "enum",
        "defaultValue",
        "required"
})
@Reflectable
public record ApiOptionSpec(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("type") String type,
        @JsonProperty("enum") EnumSpec enumSpec,
        @JsonProperty("defaultValue") String defaultValue,
        @JsonProperty("required") boolean required) {

    @SuppressWarnings("rawtypes")
    private static final Map<String, Class> TYPES;

    /**
     * Creates a new {@link ApiOptionSpec} instance.
     */
    @ConstructorProperties({
            "name",
            "description",
            "type",
            "enum",
            "defaultValue",
            "required"
    })
    public ApiOptionSpec {
    }

    /**
     * Creates a new {@link ApiOptionSpec} instance.
     *
     * @param name         the name of the parameter.
     * @param description  the description of the parameter.
     * @param type         the type of the parameter.
     * @param defaultValue the default value of the parameter.
     * @param required     specifies if the parameter is required.
     */
    public ApiOptionSpec(String name,
                         String description,
                         Class<?> type,
                         String defaultValue,
                         boolean required) {
        this(name, description, getTypeString(type), getEnumSpec(type), defaultValue, required);
    }

    @JsonPropertyOrder({
            "name",
            "symbols"
    })
    @Reflectable
    public record EnumSpec(@JsonProperty("name") String name,
                           @JsonProperty("symbols") Set<String> symbols) {
        @ConstructorProperties({
                "name",
                "symbols"
        })
        public EnumSpec {

        }
    }

    public static final Class<String> DEFAULT_TYPE = String.class;

    static {
        var type = new Class[]{
                Boolean.class,
                String.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                List.class,
                Map.class
        };
        TYPES = Arrays.stream(type)
                .collect(toMap(
                        Class::getSimpleName,
                        Function.identity())
                );
    }


    public Class<?> typeClass() {
        return TYPES.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(type))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_TYPE);
    }

    private static EnumSpec getEnumSpec(final Class<?> clazz) {
        if (Enum.class.isAssignableFrom(clazz)) {
            return new EnumSpec(
                    clazz.getSimpleName(),
                    Arrays.stream(((Class<Enum<?>>) clazz).getEnumConstants())
                            .map(Enum::name)
                            .collect(Collectors.toSet())
            );
        }
        return null;
    }

    private static String getTypeString(final Class<?> clazz) {
        return TYPES.containsKey(clazz.getSimpleName()) ?
                clazz.getSimpleName() :
                DEFAULT_TYPE.getSimpleName();
    }
}
