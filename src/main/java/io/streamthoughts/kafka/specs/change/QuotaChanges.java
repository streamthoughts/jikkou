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
package io.streamthoughts.kafka.specs.change;

import io.streamthoughts.kafka.specs.OperationResult;
import io.streamthoughts.kafka.specs.model.V1QuotaEntityObject;
import io.streamthoughts.kafka.specs.model.V1QuotaObject;
import io.streamthoughts.kafka.specs.model.V1QuotaType;
import io.streamthoughts.kafka.specs.operation.quotas.QuotaOperation;
import io.streamthoughts.kafka.specs.resources.Configs;
import io.vavr.Tuple2;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.streamthoughts.kafka.specs.internal.FutureUtils.makeCompletableFuture;

public class QuotaChanges implements Changes<QuotaChange, QuotaOperation> {

    public static QuotaChanges computeChanges(@NotNull final Iterable<V1QuotaObject> beforeQuotasObjects,
                                              @NotNull final Iterable<V1QuotaObject> afterQuotasObjects) {

        final Map<ClientQuotaKey, V1QuotaObject> beforeQuotasMapByEntity = StreamSupport
                .stream(beforeQuotasObjects.spliterator(), false)
                .collect(Collectors.toMap(it -> newClientQuotaKey(it.type(), it.entity()), o -> o));

        final Map<ClientQuotaKey, QuotaChange> changes = new HashMap<>();

        for (final V1QuotaObject afterQuota : afterQuotasObjects) {

            final ClientQuotaKey key = newClientQuotaKey(afterQuota.type(), afterQuota.entity());

            final V1QuotaObject beforeQuota = beforeQuotasMapByEntity.get(key);
            final QuotaChange change = beforeQuota == null ?
                    buildChangeForNewQuota(afterQuota) :
                    buildChangeForExistingQuota(beforeQuota, afterQuota);

            changes.put(
                    newClientQuotaKey(change.getType(), change.getEntity()),
                    change
            );
        }

        Map<ClientQuotaKey, QuotaChange> changeForDeletedQuotas = buildChangesForOrphanQuotas(
                beforeQuotasMapByEntity.values(),
                changes.keySet()
        );

        changes.putAll(changeForDeletedQuotas);

        return new QuotaChanges(changes);

    }

    @NotNull
    private static QuotaChanges.ClientQuotaKey newClientQuotaKey(final V1QuotaType type,
                                                                 final V1QuotaEntityObject entity) {
        return new ClientQuotaKey(type, type.toEntities(entity));
    }

    public static QuotaChange buildChangeForNewQuota(@NotNull final V1QuotaObject quota) {
        final List<ConfigEntryChange> configChanges = quota.configs()
                .toMapDouble()
                .entrySet()
                .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withAfterValue(it.getValue())))
                .collect(Collectors.toList());
        return new QuotaChange(Change.OperationType.ADD, quota.type(), quota.entity(), configChanges);
    }


    public static QuotaChange buildChangeForExistingQuota(@NotNull final V1QuotaObject before,
                                                          @NotNull final V1QuotaObject after) {

        final Tuple2<Change.OperationType, List<ConfigEntryChange>> computed = ConfigEntryChanges.computeChange(
                Configs.of(before.configs().toMapDouble()),
                Configs.of(after.configs().toMapDouble())
        );
        return new QuotaChange(computed._1(), before.type(), before.entity(), computed._2());
    }

    private static @NotNull Map<ClientQuotaKey, QuotaChange> buildChangesForOrphanQuotas(
            @NotNull final Collection<V1QuotaObject> quotas,
            @NotNull final Set<ClientQuotaKey> changes) {
        return quotas
                .stream()
                .filter(it -> !changes.contains(newClientQuotaKey(it.type(), it.entity())))
                .map(quota -> {
                    final List<ConfigEntryChange> configChanges = quota.configs()
                            .toMapDouble()
                            .entrySet()
                            .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withBeforeValue(it.getValue())))
                            .collect(Collectors.toList());

                    return new QuotaChange(
                            Change.OperationType.DELETE,
                            quota.type(),
                            quota.entity(),
                            configChanges

                    );
                })
                .collect(Collectors.toMap(it -> newClientQuotaKey(it.getType(), it.getEntity()), it -> it));
    }

    private final Map<ClientQuotaKey, QuotaChange> changes;

    /**
     * Creates a new {@link QuotaChanges} instance.
     *
     * @param changes the changes.
     */
    public QuotaChanges(@NotNull final Map<ClientQuotaKey, QuotaChange> changes) {
        this.changes = changes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<QuotaChange> all() {
        return new ArrayList<>(changes.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OperationResult<QuotaChange>> apply(final @NotNull QuotaOperation operation) {

        Map<ClientQuotaKey, QuotaChange> filtered = filter(operation)
                .stream()
                .collect(Collectors.toMap(
                        it -> newClientQuotaKey(it.getType(), it.getEntity()),
                        it -> it
                ));

        Map<ClientQuotaEntity, KafkaFuture<Void>> results = operation.apply(new QuotaChanges(filtered));

        List<CompletableFuture<OperationResult<QuotaChange>>> completableFutures = results.entrySet()
                .stream()
                .map(entry -> {
                    final Future<Void> future = entry.getValue();
                    ClientQuotaEntity clientQuotaEntity = entry.getKey();
                    Map<String, String> entries = clientQuotaEntity.entries();
                    V1QuotaType type = V1QuotaType.from(entries);
                    return makeCompletableFuture(future, changes.get(new ClientQuotaKey(type, entries)), operation);
                }).collect(Collectors.toList());

        return completableFutures
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * Class used for identifying a unique client-quota.
     */
    private static final class ClientQuotaKey {

        private final V1QuotaType type;
        private final Map<String, String> entities;

        public ClientQuotaKey(@NotNull final V1QuotaType type,
                              @NotNull final Map<String, String> entities) {
            this.type = type;
            this.entities = entities;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientQuotaKey quotaKey = (ClientQuotaKey) o;
            return type == quotaKey.type && Objects.equals(entities, quotaKey.entities);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, entities);
        }

        @Override
        public String toString() {
            return "ClientQuotaKey{" +
                    "type=" + type +
                    ", entities=" + entities +
                    '}';
        }
    }
}
