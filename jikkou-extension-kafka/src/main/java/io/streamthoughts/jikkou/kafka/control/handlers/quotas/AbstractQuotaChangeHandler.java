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
package io.streamthoughts.jikkou.kafka.control.handlers.quotas;

import io.streamthoughts.jikkou.api.control.ChangeMetadata;
import io.streamthoughts.jikkou.api.control.ChangeResponse;
import io.streamthoughts.jikkou.kafka.control.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterClientQuotasResult;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractQuotaChangeHandler implements KafkaQuotaChangeHandler {

    private final AdminClient client;

    /**
     * Creates a new {@link AlterQuotasChangeHandlerKafka} instance.
     *
     * @param client the {@link AdminClient}.
     */
    public AbstractQuotaChangeHandler(@NotNull final AdminClient client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<QuotaChange>> apply(@NotNull final List<QuotaChange> changes) {
        final List<ClientQuotaAlteration> alterations = changes
                .stream().map(quota -> {
                    final ClientQuotaEntity entity = new ClientQuotaEntity(quota.getType().toEntities(quota.getEntity()));
                    final List<ClientQuotaAlteration.Op> operations = quota.getConfigs()
                            .stream()
                            .map(it -> new ClientQuotaAlteration.Op(it.getName(), (Double) it.getValueChange().getAfter()))
                            .collect(Collectors.toList());
                    return new ClientQuotaAlteration(entity, operations);
                }).collect(Collectors.toList());

        final AlterClientQuotasResult result = client.alterClientQuotas(alterations);

        final Map<ClientQuotaEntity, CompletableFuture<Void>> results = result.values()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        Map<ClientQuotaEntity, QuotaChange> changeByQuotaEntity = changes.stream()
                .collect(Collectors.toMap(c -> new ClientQuotaEntity(c.getType().toEntities(c.getEntity())), c -> c));

        return results.entrySet()
                .stream()
                .map(e -> new ChangeResponse<>(
                                changeByQuotaEntity.get(e.getKey()),
                                e.getValue().thenApply(unused -> ChangeMetadata.empty())
                        )
                )
                .toList();
    }
}
