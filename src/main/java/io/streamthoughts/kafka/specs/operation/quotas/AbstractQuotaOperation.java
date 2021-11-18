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
package io.streamthoughts.kafka.specs.operation.quotas;

import io.streamthoughts.kafka.specs.change.Change;
import io.streamthoughts.kafka.specs.change.QuotaChange;
import io.vavr.Tuple2;
import io.vavr.concurrent.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterClientQuotasResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractQuotaOperation implements QuotaOperation {

    private final AdminClient client;

    private final Change.OperationType operationType;

    private final boolean deleteConfigOrphans;

    /**
     * Creates a new {@link AlterQuotasOperation} instance.
     *
     * @param client        the {@link AdminClient}.
     * @param operationType the supported operation-type.
     */
    public AbstractQuotaOperation(@NotNull final AdminClient client,
                                  @NotNull final Change.OperationType operationType,
                                  final boolean deleteConfigOrphans) {
        this.client = client;
        this.operationType = operationType;
        this.deleteConfigOrphans = deleteConfigOrphans;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(@NotNull final QuotaChange change) {
        return change.getOperation() == operationType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Map<ClientQuotaEntity, List<Future<Void>>> doApply(@NotNull final Collection<QuotaChange> changes) {
        final List<ClientQuotaAlteration> alterations = changes
                .stream().map(quota -> {
                    final ClientQuotaEntity entity = new ClientQuotaEntity(quota.getType().toEntities(quota.getEntity()));
                    final List<ClientQuotaAlteration.Op> operations = quota.getConfigs()
                            .stream()
                            .filter(it -> it.getOperation() != Change.OperationType.DELETE || deleteConfigOrphans)
                            .map(it -> new ClientQuotaAlteration.Op(it.name(), (Double) it.getAfter()))
                            .collect(Collectors.toList());
                    return new ClientQuotaAlteration(entity, operations);
                }).collect(Collectors.toList());

        final AlterClientQuotasResult result = client.alterClientQuotas(alterations);

        final Map<ClientQuotaEntity, KafkaFuture<Void>> kafkaResults = result.values();
        return kafkaResults.entrySet()
                .stream()
                .map(e -> new Tuple2<>(e.getKey(), List.of(Future.fromJavaFuture(e.getValue()))))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
    }
}
