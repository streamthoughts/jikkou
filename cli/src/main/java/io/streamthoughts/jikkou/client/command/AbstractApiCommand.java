/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class AbstractApiCommand extends CLIBaseCommand implements Callable<Integer> {

    /**
     * The resource options (optional).
     */
    private final Map<String, Object> options = new HashMap<>();

    /**
     * Sets the option for the specified name and value.
     *
     * @param spec  The option spec.
     * @param value The option value.
     * @return      The previous value.
     * @param <T>   The option type.
     */
    @SuppressWarnings("unchecked")
    public <T> T option(final ApiOptionSpec spec, final T value) {
        final String name = spec.name();

        if (value == null) { // Initialize value
            return (T) this.options.put(name, null);
        }

        if (isClassList(spec.typeClass())) {
            List<Object> elements = (List<Object>) this.options.computeIfAbsent(name, unused -> new ArrayList<>());
            if (value instanceof List list) {
                elements.addAll(list);
            } else {
                // not sure - can this really happen with picocli ?
                elements.add(value);
            }
            return (T) elements;
        }
        if (isClassEnum(spec.typeClass())) {
            return (T) this.options.put(name, ((Enum)value).name());
        }
        return (T) this.options.put(name, value);
    }

    private static boolean isClassEnum(Class<?> clazz) {
        return Enum.class.isAssignableFrom(clazz);
    }

    private static boolean isClassList(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    public Map<String, Object> options() {
        return options;
    }
}
