/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.quota;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.data.TypeConverter;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.core.reconciler.change.BaseChangeHandler;
import io.streamthoughts.jikkou.kafka.internals.Futures;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterClientQuotasResult;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

public final class KafkaClientQuotaChangeHandler extends BaseChangeHandler<ResourceChange> {

    private final AdminClient client;

    /**
     * Creates a new {@link KafkaClientQuotaChangeHandler} instance.
     *
     * @param client the {@link AdminClient}.
     */
    public KafkaClientQuotaChangeHandler(@NotNull final AdminClient client) {
        this(client, Set.of(Operation.CREATE, Operation.DELETE, Operation.UPDATE));
    }

    /**
     * Creates a new {@link KafkaClientQuotaChangeHandler} instance.
     *
     * @param client              the {@link AdminClient}.
     * @param supportedOperations the set of supported change type.
     */
    public KafkaClientQuotaChangeHandler(@NotNull final AdminClient client,
                                         @NotNull final Set<Operation> supportedOperations) {
        super(supportedOperations);
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<ResourceChange>> handleChanges(@NotNull final List<ResourceChange> changes) {
        final List<ClientQuotaAlteration> alterations = changes
                .stream()
                .map(item -> {
                    List<? extends StateChange> states = item.getSpec().getChanges().all();
                    final ClientQuotaEntity entity = getClientQuotaEntity(item);
                    final List<ClientQuotaAlteration.Op> operations = states
                            .stream()
                            .map(change -> new ClientQuotaAlteration.Op(
                                    change.getName(),
                                    (Double) change.getAfter()
                            ))
                            .collect(Collectors.toList());
                    return new ClientQuotaAlteration(entity, operations);
                }).collect(Collectors.toList());

        final AlterClientQuotasResult result = client.alterClientQuotas(alterations);

        final Map<ClientQuotaEntity, CompletableFuture<Void>> results = result.values()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        Map<ClientQuotaEntity, ResourceChange> changeByQuotaEntity = changes
                .stream()
                .collect(Collectors.toMap(
                        KafkaClientQuotaChangeHandler::getClientQuotaEntity,
                        c -> c)
                );

        return results.entrySet()
                .stream()
                .map(e -> new ChangeResponse<>(
                                changeByQuotaEntity.get(e.getKey()),
                                e.getValue().thenApply(unused -> ChangeMetadata.empty())
                        )
                )
                .toList();
    }

    @NotNull
    private static ClientQuotaEntity getClientQuotaEntity(ResourceChange resource) {
        Map<String, String> entries = TypeConverter.<String, String>ofMap()
            .convertValue(resource.getSpec().getData())
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry).mapRight(v -> v.isEmpty() ? null : v))
            .collect(HashMap::new, (m, v) -> m.put(v._1(), v._2()), HashMap::putAll);
        return new ClientQuotaEntity(entries);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull final ResourceChange change) {
        return new KafkaClientQuotaChangeDescription(change);
    }
}
