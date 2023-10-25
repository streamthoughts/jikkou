/*
 * Copyright 2021 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.change.handlers.quotas;

import io.streamthoughts.jikkou.core.models.HasMetadataChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeDescription;
import io.streamthoughts.jikkou.core.reconcilier.ChangeMetadata;
import io.streamthoughts.jikkou.core.reconcilier.ChangeResponse;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.internals.Futures;
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

public abstract class AbstractQuotaChangeHandler implements KafkaQuotaChangeHandler {

    private final AdminClient client;
    protected final Set<ChangeType> supportedChangeTypes;

    /**
     * Creates a new {@link UpdateQuotasChangeHandlerKafka} instance.
     *
     * @param client the {@link AdminClient}.
     */
    public AbstractQuotaChangeHandler(@NotNull final AdminClient client,
                                      @NotNull final ChangeType supportedChangeType) {
        this(client, Set.of(supportedChangeType));
    }

    /**
     * Creates a new {@link UpdateQuotasChangeHandlerKafka} instance.
     *
     * @param client the {@link AdminClient}.
     * @param supportedChangeTypes the set of supported change type.
     */
    public AbstractQuotaChangeHandler(@NotNull final AdminClient client,
                                      @NotNull final Set<ChangeType> supportedChangeTypes) {
        this.client = client;
        this.supportedChangeTypes = supportedChangeTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<ChangeResponse<QuotaChange>> apply(@NotNull final List<HasMetadataChange<QuotaChange>> items) {
        final List<ClientQuotaAlteration> alterations = items
                .stream().map(item -> {
                    QuotaChange change = item.getChange();
                    final ClientQuotaEntity entity = new ClientQuotaEntity(change.getType().toEntities(change.getEntity()));
                    final List<ClientQuotaAlteration.Op> operations = change.getConfigEntryChanges()
                            .stream()
                            .map(it -> new ClientQuotaAlteration.Op(it.name(), (Double) it.valueChange().getAfter()))
                            .collect(Collectors.toList());
                    return new ClientQuotaAlteration(entity, operations);
                }).collect(Collectors.toList());

        final AlterClientQuotasResult result = client.alterClientQuotas(alterations);

        final Map<ClientQuotaEntity, CompletableFuture<Void>> results = result.values()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Futures.toCompletableFuture(e.getValue())));

        Map<ClientQuotaEntity, HasMetadataChange<QuotaChange>> changeByQuotaEntity = items.stream()
                .collect(Collectors.toMap(
                        it -> new ClientQuotaEntity(it.getChange().getType().toEntities(it.getChange().getEntity())),
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

    /** {@inheritDoc} */
    @Override
    public Set<ChangeType> supportedChangeTypes() {
        return supportedChangeTypes;
    }

    /** {@inheritDoc} */
    @Override
    public ChangeDescription getDescriptionFor(@NotNull final HasMetadataChange<QuotaChange> item) {
        return new QuotaChangeDescription(item);
    }
}
