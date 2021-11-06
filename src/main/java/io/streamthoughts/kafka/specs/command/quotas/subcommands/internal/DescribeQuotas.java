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
package io.streamthoughts.kafka.specs.command.quotas.subcommands.internal;

import io.streamthoughts.kafka.specs.command.topic.subcommands.internal.DescribeTopics;
import io.streamthoughts.kafka.specs.error.ExecutionException;
import io.streamthoughts.kafka.specs.model.V1QuotaLimitsObject;
import io.streamthoughts.kafka.specs.model.V1QuotaObject;
import io.streamthoughts.kafka.specs.model.V1QuotaEntityObject;
import io.streamthoughts.kafka.specs.model.V1QuotaType;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClientQuotasResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.apache.kafka.common.quota.ClientQuotaFilter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DescribeQuotas {

    private final AdminClient client;

    /**
     * Creates a new {@link DescribeTopics} instance.
     *
     * @param client       the {@link AdminClient}.
     */
    public DescribeQuotas(final AdminClient client) {
        this.client = client;
    }

    public List<V1QuotaObject> describe() {
        DescribeClientQuotasResult result = client.describeClientQuotas(ClientQuotaFilter.all());
        KafkaFuture<Map<ClientQuotaEntity, Map<String, Double>>> future = result.entities();
        try {
            Map<ClientQuotaEntity, Map<String, Double>> entities = future.get();
            return entities.entrySet()
                    .stream()
                    .map(e -> {
                        Map<String, String> entries = e.getKey().entries();
                        V1QuotaEntityObject entityObject = new V1QuotaEntityObject(
                                entries.get(ClientQuotaEntity.USER),
                                entries.get(ClientQuotaEntity.CLIENT_ID)
                        );
                        V1QuotaLimitsObject configsObject = new V1QuotaLimitsObject(e.getValue());
                        return new V1QuotaObject(V1QuotaType.from(entries), entityObject, configsObject);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
}
