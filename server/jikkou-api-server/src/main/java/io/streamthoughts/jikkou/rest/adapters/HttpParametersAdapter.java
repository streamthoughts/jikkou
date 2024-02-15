/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.adapters;

import io.micronaut.http.HttpParameters;
import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class HttpParametersAdapter {

    @NotNull
    public static Configuration toConfiguration(@NotNull HttpParameters parameters) {
        return Configuration.from(toMap(parameters));
    }

    @NotNull
    public static Map<String, Object> toMap(@NotNull HttpParameters parameters) {
        Map<String, Object> result = new HashMap<>();
        for (String name : parameters.names()) {
            List<String> values = parameters.getAll(name);
            if (values.isEmpty()) {
                result.put(name, null);
            } else if (values.size() == 1) {
                result.put(name, values.get(0));
            } else {
                result.put(name, values);
            }
        }
        return result;
    }
}
