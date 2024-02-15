/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.internals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

public class ConfigsBuilder {

    private final List<ResourceConfigSupplier> suppliers = new ArrayList<>();

    public ResourceConfigSupplier newResourceConfig() {
        ResourceConfigSupplier newSupplier = new ResourceConfigSupplier();
        suppliers.add(newSupplier);
        return newSupplier;
    }

    public static class ResourceConfigSupplier {
        private final Map<String, String> entries = new HashMap<>();

        private ConfigResource.Type type;

        private String name;

        public ResourceConfigSupplier setType(final ConfigResource.Type type) {
            this.type = type;
            return this;
        }

        public ResourceConfigSupplier setName(final String name) {
            this.name = name;
            return this;
        }

        public ResourceConfigSupplier setConfig(final String key, String value) {
            entries.put(key, value);
            return this;
        }

        void supply(final Map<ConfigResource, Config> config) {
            List<ConfigEntry> configEntries = entries.entrySet()
                    .stream().map(e -> new ConfigEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            config.put(new ConfigResource(type, name), new Config(configEntries));
        }
    }

    public Map<ConfigResource, Config> build() {
        Map<ConfigResource, Config> configs = new HashMap<>();
        suppliers.forEach(s -> s.supply(configs));
        return configs;
    }
}