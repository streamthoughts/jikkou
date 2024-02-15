/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.core.models.ConfigValue;
import io.streamthoughts.jikkou.core.models.Configs;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;

public final class KafkaConfigsAdapter {

    /**
     * Static helper method to create a new {@link Configs} object from a given Kafka {@link Config}.
     * A {@link Predicate} can be passed to filter config entries.
     *
     * @param config        the {@link Config}.
     * @param predicate     the {@link Predicate}.
     * @return              a new {@link Configs}.
     */
    public static Configs of(final Config config,
                             final Predicate<ConfigEntry> predicate) {
        return new Configs(config.entries().stream()
                .filter(predicate)
                .map(KafkaConfigsAdapter::of)
                .collect(Collectors.toSet())
        );
    }

    /**
     * Static helper method to create a new {@link ConfigValue} object from a given Kafka {@link ConfigEntry}.
     *
     * @param configEntry   the {@link ConfigEntry}.
     * @return              a new {@link ConfigValue}.
     */
    public static ConfigValue of(final ConfigEntry configEntry) {
        return new ConfigValue(
                configEntry.name(),
                configEntry.value(),
                configEntry.isDefault(),
                configEntry.source() == ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG
        );
    }

}
