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
import java.util.Set;

/**
 * Options used for computing quota changes.
 *
 * @see QuotaChangeComputer
 */

public final class KafkaQuotaReconciliationConfig
        extends ConfigurationSupport<KafkaQuotaReconciliationConfig> {

    public static final ConfigProperty<Boolean> DELETE_QUOTA_ORPHANS_OPTION =
            ConfigProperty.ofBoolean(  "delete-quota-orphans").orElse(false);


    /**
     * Creates a new {@link KafkaQuotaReconciliationConfig} instance.
     */
    public KafkaQuotaReconciliationConfig() {
        this(Configuration.empty());
    }

    /**
     * Creates a new {@link KafkaQuotaReconciliationConfig} instance.
     *
     * @param configuration the configuration to be used.
     */
    public KafkaQuotaReconciliationConfig(final Configuration configuration) {
        configure(configuration);
    }

    public boolean isDeleteQuotaOrphans() {
        return get(DELETE_QUOTA_ORPHANS_OPTION);
    }

    /** {@inheritDoc} **/
    @Override
    protected KafkaQuotaReconciliationConfig newInstance(Configuration configuration) {
        return new KafkaQuotaReconciliationConfig(configuration);
    }

    /** {@inheritDoc} **/
    @Override
    protected Set<ConfigProperty<?>> defaultConfigProperties() {
        return Set.of(DELETE_QUOTA_ORPHANS_OPTION);
    }
}
