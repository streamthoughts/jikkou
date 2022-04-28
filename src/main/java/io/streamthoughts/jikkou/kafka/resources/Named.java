/*
 * Copyright 2020 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Default interface to name an entity.
 */
public interface Named {

    /**
     * @return the name of the entity.
     */
    @JsonProperty
    String name();

    /**
     * Helper method to key all {@link Named} resources by name.
     *
     * @param resources the list of resources.
     * @param <T>       the type.
     * @return          the Map of object keyed by name.
     */
    static <T extends Named> Map<String, T> keyByName(final Iterable<T> resources) {
        if (resources == null) return Collections.emptyMap();

        return new TreeMap<>(
            StreamSupport
            .stream(resources.spliterator(), false)
            .collect(Collectors.toMap(Named::name, o -> o))
        );
    }

    /**
     * Helper method to key all {@link Named} resources by name.
     *
     * @param resources the list of resources.
     * @param <T>       the type.
     * @return          the Map of object keyed by name.
     */
    static <T extends Named> Map<String, List<T>> groupByName(final Iterable<T> resources) {
        if (resources == null) return Collections.emptyMap();

        return StreamSupport
            .stream(resources.spliterator(), false)
            .collect(Collectors.groupingBy(Named::name));
    }
}
