/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.core.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Configuration} that is backed by a {@link Map}.
 */
class MapConfiguration implements Configuration {

    private final Map<String, ?> configMap;
    private final Map<String, ?> fallbackConfigMap;

    /**
     * Creates a new {@link MapConfiguration}.
     *
     * @param configs  the config map to be used for creating this configuration.
     */
    public MapConfiguration(@NotNull final Map<String, ?> configs) {
        this(configs, Collections.emptyMap());
    }

    /**
     * Creates a new {@link MapConfiguration}.
     *
     * @param configs   the config map to be used for creating this configuration.
     * @param fallback  the fallback map to be used for creating this configuration.
     */
    private MapConfiguration(@NotNull final Map<String, ?> configs, @NotNull final Map<String, ?> fallback) {
        this.configMap = new HashMap<>(configs);
        this.fallbackConfigMap =  new HashMap<>(fallback);
    }

    /**{@inheritDoc}**/
    @Override
    public Set<String> keys() {
        return configMap.keySet();
    }

    /**{@inheritDoc}**/
    @Override
    public boolean hasKey(@NotNull String key) {
        return configMap.containsKey(key) || fallbackConfigMap.containsKey(key);
    }

    /**{@inheritDoc}**/
    @Override
    public Object getAny(@NotNull String key) {
        if (configMap.containsKey(key))
            return configMap.get(key);
        return fallbackConfigMap.get(key);
    }

    /**{@inheritDoc}**/
    @Override
    public Configuration withFallback(@NotNull Configuration other) {
        return new MapConfiguration(this.configMap, other.asMap());
    }

    /**{@inheritDoc}**/
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> results = new HashMap<>(fallbackConfigMap);
        results.putAll(configMap);
        return results;
    }

    /**{@inheritDoc}**/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapConfiguration that = (MapConfiguration) o;
        return Objects.equals(configMap, that.configMap) &&
                Objects.equals(fallbackConfigMap, that.fallbackConfigMap);
    }

    /**{@inheritDoc}**/
    @Override
    public int hashCode() {
        return Objects.hash(configMap, fallbackConfigMap);
    }

    /**{@inheritDoc}**/
    @Override
    public String toString() {
        return asMap().toString();
    }
}
