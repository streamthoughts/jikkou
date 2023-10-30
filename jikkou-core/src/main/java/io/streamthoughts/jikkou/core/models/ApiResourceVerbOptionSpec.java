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
package io.streamthoughts.jikkou.core.models;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * ApiResourceVerbOptionSpec.
 *
 * @param name              the name of the parameter.
 * @param description       the description of the parameter.
 * @param type              the type of the parameter.
 * @param defaultValue      the default value of the parameter.
 * @param required          specifies if the parameter is required.
 */
@JsonPropertyOrder({
        "name",
        "description",
        "type",
        "defaultValue",
        "required"
}
)
public record ApiResourceVerbOptionSpec(
        String name,
        String description,
        String type,
        String defaultValue,
        boolean required) {

    /**
     * Creates a new {@link ApiResourceVerbOptionSpec} instance.
     */
     @ConstructorProperties({
            "name",
            "kind",
            "singularName",
            "shortNames",
            "description",
            "verbs",
            "metadata"})
    public ApiResourceVerbOptionSpec {
    }

    /**
     * Creates a new {@link ApiResourceVerbOptionSpec} instance.
     *
     * @param name              the name of the parameter.
     * @param description       the description of the parameter.
     * @param type              the type of the parameter.
     * @param defaultValue      the default value of the parameter.
     * @param required          specifies if the parameter is required.
     */
    public ApiResourceVerbOptionSpec(String name,
                                     String description,
                                     Class<?> type,
                                     String defaultValue,
                                     boolean required) {
        this(name, description, getTypeString(type), defaultValue, required);
    }

    @SuppressWarnings("rawtypes")
    private static final Map<String, Class> TYPES;
    static {
        var type = new Class[]{
                Boolean.class,
                String.class,
                Short.class,
                Integer.class,
                Long.class,
                Double.class,
                List.class
        };
        TYPES = Arrays.stream(type)
                .collect(toMap(
                        ApiResourceVerbOptionSpec::getTypeString,
                        Function.identity())
                );
    }

    public Class<?> typeClass() {
        return getTypeClass(type);
    }

    private static String getTypeString(final Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase(Locale.ROOT);
    }

    public static Class<?> getTypeClass(final String type) {
        return TYPES.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(type))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Type '" + type + "' is not supported"));
    }
}
