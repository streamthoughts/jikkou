/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.kafka.specs.reader;


import io.streamthoughts.kafka.specs.InvalidSpecificationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to manipulate Map of object.
 */
public class MapObjectReader {

    private final Map<String, Object> objectMap;

    @SuppressWarnings("unchecked")
    public static MapObjectReader toMap(final Object o) {
        return new MapObjectReader((Map<String, Object>)o);
    }

    @SuppressWarnings("unchecked")
    public static List<MapObjectReader> toList(final Object o) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) o;
        return list.stream().map(MapObjectReader::new).collect(Collectors.toList());
    }

    /**
     * Creates a new {@link MapObjectReader} instance.
     * @param map
     */
    private MapObjectReader(final Map<String, Object> map) {
        this.objectMap = map;
    }

    /**
     * Gets the object for the specified key.
     *
     * @param key   the object-key to retrieve.
     * @return      a new instance of {@link MapObjectReader}.
     * @throws InvalidSpecificationException if the specified key is missing.
     */
    public MapObjectReader getMapObject(final String key) {
        Object o = get(key);
        return o != null ? toMap(o) : new MapObjectReader(Collections.emptyMap());
    }

    public List<MapObjectReader> getMapList(final String key) {
        try {
            Object o = this.objectMap.get(key);
            return o != null ? toList(o) : Collections.emptyList();
        } catch (Exception e) {
            throw newInvalidSpecification(key, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T>  T getOrElse(String key, T defaultVal) {
        if (!objectMap.containsKey(key)) return defaultVal;
        return (T) objectMap.get(key);
    }

    /**
     * Gets the object for the specified key.
     *
     * @param key   the object-key to retrieve
     * @param <T>   the return type.
     * @return
     * @throws InvalidSpecificationException if the specified key is missing.
     */
    @SuppressWarnings("unchecked")
    public <T>  T get(final String key) {
        if (!objectMap.containsKey(key)) {
            throw new InvalidSpecificationException("missing key '" + key + "'");
        }
        try {
            return (T) objectMap.get(key);
        } catch (ClassCastException e) {
            throw newInvalidSpecification(key, e);
        }
    }

    private InvalidSpecificationException newInvalidSpecification(String key, Exception e)  {
        return new InvalidSpecificationException("Error while reading field '" + key + "' : " + e.getMessage());
    }

}