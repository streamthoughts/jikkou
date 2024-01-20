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
