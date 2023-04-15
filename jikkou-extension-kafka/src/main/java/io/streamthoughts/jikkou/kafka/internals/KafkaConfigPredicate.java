/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.internals;

import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_DEFAULT_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.apache.kafka.clients.admin.ConfigEntry;

/**
 * Predicate to filter topic's config entries.
 *
 * @see ConfigEntry
 */
public class KafkaConfigPredicate implements Predicate<ConfigEntry> {

    private boolean filterDefaultConfig = false;
    private boolean filterStaticBrokerConfig = false;
    private boolean filterDynamicBrokerConfig = false;
    private boolean filterDynamicTopicConfig = false;

    public KafkaConfigPredicate withDefaultConfig(boolean filterDefaultConfig) {
        this.filterDefaultConfig = filterDefaultConfig;
        return this;
    }

    public KafkaConfigPredicate withStaticBrokerConfig(boolean filterStaticBrokerConfig) {
        this.filterStaticBrokerConfig = filterStaticBrokerConfig;
        return this;
    }

    public KafkaConfigPredicate withDynamicBrokerConfig(boolean filterDynamicBrokerConfig) {
        this.filterDynamicBrokerConfig = filterDynamicBrokerConfig;
        return this;
    }

    public KafkaConfigPredicate withDynamicTopicConfig(boolean filterDynamicTopicConfig) {
        this.filterDynamicTopicConfig = filterDynamicTopicConfig;
        return this;
    }

    /**
     * Evaluates this predicate on the given config entry.
     *
     * @param configEntry the input config entry.
     * @return {@code true} if the input config entry matches the predicate, otherwise {@code false}
     */
    @Override
    public boolean test(ConfigEntry configEntry) {
        List<Predicate<ConfigEntry>> predicates = new ArrayList<>();

        if (filterDynamicTopicConfig) {
            predicates.add(config -> config.source() == DYNAMIC_TOPIC_CONFIG);
        }

        if (filterDefaultConfig) {
            predicates.add(ConfigEntry::isDefault);
        }

        if (filterStaticBrokerConfig) {
            predicates.add(config -> config.source() == STATIC_BROKER_CONFIG);
        }

        if (filterDynamicBrokerConfig) {
            predicates.add(config -> config.source() == DYNAMIC_BROKER_CONFIG);
            predicates.add(config -> config.source() == DYNAMIC_DEFAULT_BROKER_CONFIG);
        }

        return predicates.stream().reduce(config -> false, Predicate::or).test(configEntry);
    }
}
