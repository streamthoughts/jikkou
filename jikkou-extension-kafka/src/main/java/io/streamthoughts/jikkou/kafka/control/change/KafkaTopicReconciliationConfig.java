/*
 * Copyright 2021 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.control.change;

import io.streamthoughts.jikkou.api.config.ConfigProperty;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.config.ConfigurationSupport;
import io.streamthoughts.jikkou.api.control.ReconciliationConfig;
import java.util.Set;

/**
 * Immutable configuration for running reconciliation of Kafka topics.
 *
 * @see TopicChangeComputer
 * @see io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTopicController
 */
public class KafkaTopicReconciliationConfig extends ConfigurationSupport<KafkaTopicReconciliationConfig>
    implements ReconciliationConfig {

    public static final ConfigProperty<Boolean> DELETE_CONFIG_ORPHANS_OPTION =
            ConfigProperty.ofBoolean("delete-config-orphans").orElse(false);

    public static final ConfigProperty<Boolean> DELETE_TOPIC_ORPHANS_OPTION =
            ConfigProperty.ofBoolean("delete-topic-orphans").orElse(false);

    public static final ConfigProperty<Boolean> EXCLUDE_INTERNAL_TOPICS_OPTION =
            ConfigProperty.ofBoolean("exclude-internal-topics").orElse(true);

    /**
     * Creates a new {@link KafkaTopicReconciliationConfig} instance.
     */
    public KafkaTopicReconciliationConfig() {
        this(Configuration.empty());
    }

    /**
     * Creates a new {@link KafkaTopicReconciliationConfig} instance.
     */
    public KafkaTopicReconciliationConfig(final Configuration configuration) {
        configure(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    protected KafkaTopicReconciliationConfig newInstance(final Configuration configuration) {
        return new KafkaTopicReconciliationConfig(configuration);
    }

    public KafkaTopicReconciliationConfig withDeleteConfigOrphans(boolean deleteConfigOrphans) {
        return with(DELETE_CONFIG_ORPHANS_OPTION, deleteConfigOrphans);
    }

    public KafkaTopicReconciliationConfig withDeleteTopicOrphans(boolean deleteTopicOrphans) {
        return with(DELETE_TOPIC_ORPHANS_OPTION, deleteTopicOrphans);
    }

    public KafkaTopicReconciliationConfig withExcludeInternalTopics(boolean excludeInternalTopics) {
        return with(EXCLUDE_INTERNAL_TOPICS_OPTION, excludeInternalTopics);
    }

    public boolean isDeleteConfigOrphans() {
        return get(DELETE_CONFIG_ORPHANS_OPTION);
    }

    public boolean isDeleteTopicOrphans() {
        return get(DELETE_TOPIC_ORPHANS_OPTION);
    }

    public boolean isExcludeInternalTopics() {
        return get(EXCLUDE_INTERNAL_TOPICS_OPTION);
    }

    /** {@inheritDoc} **/
    @Override
    public Set<ConfigProperty<?>> defaultConfigProperties() {
        return Set.of(
                DELETE_CONFIG_ORPHANS_OPTION,
                DELETE_TOPIC_ORPHANS_OPTION,
                EXCLUDE_INTERNAL_TOPICS_OPTION
        );
    }
}
