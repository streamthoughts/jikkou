/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reporter.ce;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/** Represents a list of Cloud Events Extension. */
@FunctionalInterface
public interface CloudEventExtension {

    /** Gets the extensions. */
    Map<String, Object> toAttributesExtensions();

    /**
     * Helper method to create a new {@link CloudEventExtension} for the given key-value pair.
     *
     * @param key the extension key.
     * @param value the extension value.
     * @return a new {@link CloudEventExtension}.
     */
    static CloudEventExtension of(final String key, final Object value) {
        return () -> Collections.singletonMap(key, value);
    }

    static CloudEventExtension of(final Map<String, ?> extensions) {
        return () -> Collections.unmodifiableMap(extensions);
    }

    /**
     * Helper method to convert a collection of {@link CloudEventExtension} to Map.
     *
     * @param extensions the extensions to convert.
     * @return the {@link Map}.
     */
    static Map<String, Object> marshal(final Collection<CloudEventExtension> extensions) {
        return extensions.stream()
                .map(CloudEventExtension::toAttributesExtensions)
                .flatMap(t -> t.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
