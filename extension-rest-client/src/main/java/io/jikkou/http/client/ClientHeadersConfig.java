/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.http.client;

import io.jikkou.core.config.ConfigProperty;
import io.jikkou.core.config.Configuration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Additional HTTP headers sent on every request made by a REST-based provider.
 */
public final class ClientHeadersConfig {

    public static final ConfigProperty<Map<String, Object>> CLIENT_HEADERS = ConfigProperty
        .ofMap("clientHeaders")
        .displayName("Client Headers")
        .description("Additional HTTP headers to send on every request to the target API, "
            + "for example an API gateway key or a tracing header. These headers are applied "
            + "last and therefore override any header Jikkou sets itself, including 'Authorization'.")
        .defaultValue(Map.of());

    /**
     * Reads the custom client headers from the given configuration.
     * <p>
     * Header values are coerced to {@link String}. Entries with a blank name or a
     * {@code null} value are ignored.
     *
     * @param configuration the configuration.
     * @return an unmodifiable map of header name to header value; never {@code null}.
     */
    public static Map<String, String> from(final Configuration configuration) {
        Map<String, Object> headers = CLIENT_HEADERS.get(configuration);
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>(headers.size());
        headers.forEach((name, value) -> {
            if (name != null && !name.isBlank() && value != null) {
                result.put(name.trim(), String.valueOf(value));
            }
        });
        return Collections.unmodifiableMap(result);
    }

    private ClientHeadersConfig() {
    }
}
