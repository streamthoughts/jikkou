/*
 * Copyright 2022 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.api.model.ConfigValue;
import io.streamthoughts.jikkou.api.model.Configs;
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
