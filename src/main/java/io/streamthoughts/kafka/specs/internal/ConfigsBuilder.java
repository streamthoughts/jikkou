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
package io.streamthoughts.kafka.specs.internal;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigsBuilder {

    private List<ResourceConfigSupplier> suppliers = new ArrayList<>();

    public ResourceConfigSupplier newResourceConfig() {
        ResourceConfigSupplier newSupplier = new ResourceConfigSupplier();
        suppliers.add(newSupplier);
        return newSupplier;
    }

    public static class ResourceConfigSupplier {
        private Map<String, String> entries = new HashMap<>();

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