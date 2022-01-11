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
package io.streamthoughts.kafka.specs.manager;

import org.apache.kafka.clients.admin.ConfigEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.DYNAMIC_DEFAULT_BROKER_CONFIG;
import static org.apache.kafka.clients.admin.ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG;

public class BrokerDescribeOptions implements DescribeOptions {

    private final boolean describeDefaultConfigs;

    private final boolean describeStaticBrokerConfigs;

    private final boolean describeDynamicBrokerConfigs;

    public BrokerDescribeOptions() {
        this(true, true, true);
    }

    public BrokerDescribeOptions(final boolean describeDefaultConfigs,
                                final boolean describeStaticBrokerConfigs,
                                final boolean describeDynamicBrokerConfigs) {
        this.describeDefaultConfigs = describeDefaultConfigs;
        this.describeStaticBrokerConfigs = describeStaticBrokerConfigs;
        this.describeDynamicBrokerConfigs = describeDynamicBrokerConfigs;
    }

    public BrokerDescribeOptions withDescribeDefaultConfigs(final boolean describeDefaultConfigs) {
        return new BrokerDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs
        );
    }

    /**
     * Creates a new {@link BrokerDescribeOptions} with the given options.
     *
     * @param   describeStaticBrokerConfigs {@code true} to describe static broker configs.
     * @return  a new {@link BrokerDescribeOptions}.
     */
    public BrokerDescribeOptions withDescribeStaticBrokerConfigs(final boolean describeStaticBrokerConfigs) {
        return new BrokerDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs
        );
    }

    /**
     * Creates a new {@link BrokerDescribeOptions} with the given options.
     *
     * @param   describeDynamicBrokerConfigs {@code true}to describe dynamic broker configs.
     * @return  a new {@link BrokerDescribeOptions}.
     */
    public BrokerDescribeOptions withDescribeDynamicBrokerConfigs(final boolean describeDynamicBrokerConfigs) {
        return new BrokerDescribeOptions(
                describeDefaultConfigs,
                describeStaticBrokerConfigs,
                describeDynamicBrokerConfigs
        );
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
