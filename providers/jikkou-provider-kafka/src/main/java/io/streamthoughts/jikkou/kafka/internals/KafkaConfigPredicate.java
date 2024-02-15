/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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

    public KafkaConfigPredicate defaultConfig(boolean filterDefaultConfig) {
        this.filterDefaultConfig = filterDefaultConfig;
        return this;
    }

    public KafkaConfigPredicate staticBrokerConfig(boolean filterStaticBrokerConfig) {
        this.filterStaticBrokerConfig = filterStaticBrokerConfig;
        return this;
    }

    public KafkaConfigPredicate dynamicBrokerConfig(boolean filterDynamicBrokerConfig) {
        this.filterDynamicBrokerConfig = filterDynamicBrokerConfig;
        return this;
    }

    public KafkaConfigPredicate dynamicTopicConfig(boolean filterDynamicTopicConfig) {
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
