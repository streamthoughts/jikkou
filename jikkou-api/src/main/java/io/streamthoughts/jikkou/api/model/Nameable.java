/*
 * Copyright 2020 The original authors
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
package io.streamthoughts.jikkou.api.model;

import io.streamthoughts.jikkou.common.annotation.InterfaceStability;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Represents an object that can be named.
 */
@InterfaceStability.Evolving
public interface Nameable {

    static Nameable of(final String name) {
        return () -> name;
    }

    /**
     * @return the name of the entity.
     */
    String getName();

    /**
     * Helper method to key all {@link Nameable} resources by name.
     *
     * @param resources the list of resources.
     * @param <T>       the type.
     * @return          the Map of object keyed by name.
     */
    static <T extends Nameable> Map<String, T> keyByName(final Iterable<T> resources) {
        if (resources == null) return Collections.emptyMap();

        return new TreeMap<>(
            StreamSupport
            .stream(resources.spliterator(), false)
            .collect(Collectors.toMap(Nameable::getName, o -> o))
        );
    }

    /**
     * Helper method to key all {@link Nameable} resources by name.
     *
     * @param resources the list of resources.
     * @param <T>       the type.
     * @return          the Map of object keyed by name.
     */
    static <T extends Nameable> Map<String, List<T>> groupByName(final Iterable<T> resources) {
        if (resources == null) return Collections.emptyMap();

        return StreamSupport
            .stream(resources.spliterator(), false)
            .collect(Collectors.groupingBy(Nameable::getName));
    }

    static <T extends Nameable> List<T> sortByName(final Iterable<T> resources) {
        return StreamSupport.stream(resources.spliterator(), false)
                .sorted(Comparator.comparing(Nameable::getName))
                .toList();
    }
}
