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
package io.streamthoughts.jikkou.api.manager;

import org.apache.kafka.clients.admin.ConfigEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_DEFAULT_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG;

/**
 * Options for describing topics.
 *
 * @see KafkaTopicManager
 */
public class TopicDescribeOptions implements DescribeOptions {

    private final boolean describeDefaultConfigs;

    private final boolean describeStaticBrokerConfigs;

    private final boolean describeDynamicBrokerConfigs;

    private final Predicate<String> topicPredicate;

    /**
     * Creates a new {@link KafkaTopicManager} instance.
     */
    public TopicDescribeOptions() {
        this(true, true, true, t -> true);
    }

    public TopicDescribeOptions(final boolean describeDefaultConfigs,
                                final boolean describeStaticBrokerConfigs,
                                final boolean describeDynamicBrokerConfigs,
                                final Predicate<String> topicPredicate) {
        this.describeDefaultConfigs = describeDefaultConfigs;
        this.describeStaticBrokerConfigs = describeStaticBrokerConfigs;
        this.describeDynamicBrokerConfigs = describeDynamicBrokerConfigs;
        this.topicPredicate = topicPredicate;
    }

    /**
     * Creates a new {@link TopicDescribeOptions} with the given options.
     *
     * @param   describeDefaultConfigs {@code true} to describe default configs..
     * @return  a new {@link TopicDescribeOptions}.
     */
    public TopicDescribeOptions withDescribeDefaultConfigs(final boolean describeDefaultConfigs) {
        return new TopicDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs,
                topicPredicate
        );
    }

    /**
     * Creates a new {@link TopicDescribeOptions} with the given options.
     *
     * @param   topicPredicate  the predicate used for filtering topics.
     * @return  a new {@link TopicDescribeOptions}.
     */
    public TopicDescribeOptions withTopicPredicate(@NotNull final Predicate<String> topicPredicate) {
        return new TopicDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs,
                topicPredicate
        );
    }

    /**
     * Creates a new {@link TopicDescribeOptions} with the given options.
     *
     * @param   describeStaticBrokerConfigs {@code true} to describe static broker configs.
     * @return  a new {@link TopicDescribeOptions}.
     */
    public TopicDescribeOptions withDescribeStaticBrokerConfigs(final boolean describeStaticBrokerConfigs) {
        return new TopicDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs,
                topicPredicate
        );
    }

    /**
     * Creates a new {@link TopicDescribeOptions} with the given options.
     *
     * @param   describeDynamicBrokerConfigs {@code true}to describe dynamic broker configs.
     * @return  a new {@link TopicDescribeOptions}.
     */
    public TopicDescribeOptions withDescribeDynamicBrokerConfigs(final boolean describeDynamicBrokerConfigs) {
        return new TopicDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs,
                topicPredicate
        );
    }

    public Predicate<String> topicPredicate() {
        return topicPredicate;
    }

    public Predicate<ConfigEntry> configEntryPredicate() {
        List<Predicate<ConfigEntry>> predicates = new ArrayList<>();

        predicates.add(entry -> !entry.isDefault() || describeDefaultConfigs);

        if (!describeStaticBrokerConfigs) {
            predicates.add(config -> config.source() != STATIC_BROKER_CONFIG);
        }

        if (!describeDynamicBrokerConfigs) {
            predicates.add(config -> config.source() != DYNAMIC_BROKER_CONFIG);
            predicates.add(config -> config.source() != DYNAMIC_DEFAULT_BROKER_CONFIG);
        }

        return predicates.stream().reduce(t -> true, Predicate::and);
    }
}
