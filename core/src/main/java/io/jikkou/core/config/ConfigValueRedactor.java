/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Redacts the values of sensitive configuration properties (e.g. passwords, secrets, tokens)
 * so that a configuration can be safely logged.
 */
final class ConfigValueRedactor {

    static final String REDACTED = "******";

    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "password|secret|token|private[._-]?key|credential|jaas|user[._-]?info", Pattern.CASE_INSENSITIVE);

    private ConfigValueRedactor() {}

    /**
     * Redacts the given value if its key is sensitive. Otherwise, maps and lists are traversed
     * recursively so that sensitive entries nested in structured values (e.g. a list of
     * credentials objects) are redacted too.
     *
     * @param key   the property key.
     * @param value the property value.
     * @return the value, or a redacted copy of it.
     */
    static Object redact(@NotNull final String key, final Object value) {
        if (SENSITIVE_KEY.matcher(key).find()) {
            return REDACTED;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> redacted = new LinkedHashMap<>();
            map.forEach((k, v) -> redacted.put(k, redact(String.valueOf(k), v)));
            return redacted;
        }
        if (value instanceof List<?> list) {
            // List elements have no key of their own, so only their nested entries can be sensitive.
            return list.stream().map(v -> redact("", v)).toList();
        }
        return value;
    }
}
