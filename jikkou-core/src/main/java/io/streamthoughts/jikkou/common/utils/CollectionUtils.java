/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.common.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Utility class to manipulate collections.
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Helper method to key all objects by a computed key.
     *
     * @param items the list of items.
     * @param <V>   the value-type.
     * @return the Map of keyed objects.
     */
    public static <K, V> Map<K, V> keyBy(final Iterable<V> items, final Function<V, K> keyMapper) {
        if (items == null) return Collections.emptyMap();
        return new TreeMap<>(StreamSupport
                .stream(items.spliterator(), false)
                .collect(Collectors.toMap(keyMapper, it -> it))
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends E, E> List<T> cast(List<E> list) {
        return (List) list;
    }

    public static void toFlattenMap(final Map<String, Object> source,
                                    final Map<String, Object> result) {
        toFlattenMap(source, result, null);
    }

    @SuppressWarnings("unchecked")
    public static void toFlattenMap(final Map<String, Object> source,
                                    final Map<String, Object> result,
                                    final String key) {
        source.forEach((k, v) -> {
            final String currKey = key == null ? k : key + '.' + k;
            if (v instanceof Map) {
                toFlattenMap((Map<String, Object>) v, result, currKey);
            } else if (v instanceof List) {
                final List<Object> list = (List<Object>) v;
                for (int i = 0, size = list.size(); i < size; i++) {
                    result.put(currKey + '[' + (i + 1) + ']', list.get(i));
                }
            } else {
                result.put(currKey, v);
            }
        });
    }

    public static void toNestedMap(final Map<String, Object> source,
                                   final Map<String, Object> result,
                                   final String key) {

        source.forEach((k, v) -> {
            if (k.contains(".")) {
                String[] parts = k.split("\\.", 2);
                k = parts[0];
                v = Map.of(parts[1], v);
            }

            final String path = key == null ? k : key + "." + k;

            if (v instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) v;
                Map<String, Object> nestedMap;
                if (result.containsKey(k)) {
                    Object nested = result.get(k);
                    if (nested instanceof Map) {
                        // create a new Map to make sure it is not immutable
                        nestedMap = new HashMap<>((Map<String, Object>) nested);
                    } else {
                        throw new IllegalArgumentException("Duplicate key: " + path);
                    }
                } else {
                    nestedMap = new HashMap<>();
                }
                toNestedMap(mapValue, nestedMap, path);
                result.put(k, nestedMap);
                return;
            }

            if (result.containsKey(k)) {
                throw new IllegalArgumentException("Duplicate key: " + path);
            }
            result.put(k, v);
        });
    }
}