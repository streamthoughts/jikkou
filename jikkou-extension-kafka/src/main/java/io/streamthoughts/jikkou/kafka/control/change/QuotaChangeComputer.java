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
package io.streamthoughts.jikkou.kafka.control.change;

import io.streamthoughts.jikkou.api.control.ChangeComputer;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.api.control.ConfigEntryChangeComputer;
import io.streamthoughts.jikkou.api.control.ConfigEntryReconciliationConfig;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.kafka.adapters.KafkaQuotaLimitsAdapter;
import io.streamthoughts.jikkou.kafka.model.QuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaObject;
import io.streamthoughts.jikkou.kafka.models.V1QuotaEntityObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

public class QuotaChangeComputer implements ChangeComputer<V1KafkaQuotaObject, ClientQuotaEntity, QuotaChange, KafkaQuotaReconciliationConfig> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuotaChange> computeChanges(@NotNull final Iterable<V1KafkaQuotaObject> actualQuotasObjects,
                                            @NotNull final Iterable<V1KafkaQuotaObject> expectedQuotasObjects,
                                            @NotNull final KafkaQuotaReconciliationConfig configuration) {

        final Map<ClientQuotaEntity, V1KafkaQuotaObject> actualQuotasMapByEntity = StreamSupport
                .stream(actualQuotasObjects.spliterator(), false)
                .collect(Collectors.toMap(it -> newClientQuotaEntity(it.getType(), it.getEntity()), o -> o));

        final Map<ClientQuotaEntity, QuotaChange> changes = new HashMap<>();

        for (final V1KafkaQuotaObject expectedQuota : expectedQuotasObjects) {

            final ClientQuotaEntity key = newClientQuotaEntity(expectedQuota.getType(), expectedQuota.getEntity());

            final V1KafkaQuotaObject actualQuota = actualQuotasMapByEntity.get(key);
            final QuotaChange change = actualQuota == null ?
                    buildChangeForNewQuota(expectedQuota) :
                    buildChangeForExistingQuota(actualQuota, expectedQuota, configuration);

            changes.put(
                    newClientQuotaEntity(change.getType(), change.getEntity()),
                    change
            );
        }

        if (configuration.isDeleteQuotaOrphans()) {
            changes.putAll(buildChangesForOrphanQuotas(actualQuotasMapByEntity.values(), changes.keySet()));
        }

        return new ArrayList<>(changes.values());
    }

    @NotNull
    private static ClientQuotaEntity newClientQuotaEntity(final QuotaType type,
                                                          final V1QuotaEntityObject entity) {
        return new ClientQuotaEntity(type.toEntities(entity));
    }

    public static QuotaChange buildChangeForNewQuota(@NotNull final V1KafkaQuotaObject quota) {
        final List<ConfigEntryChange> configChanges = new KafkaQuotaLimitsAdapter(quota.getConfigs())
                .toMapDouble()
                .entrySet()
                .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withAfterValue(it.getValue())))
                .collect(Collectors.toList());
        return new QuotaChange(ChangeType.ADD, quota.getType(), quota.getEntity(), configChanges);
    }


    public static QuotaChange buildChangeForExistingQuota(@NotNull final V1KafkaQuotaObject actualState,
                                                          @NotNull final V1KafkaQuotaObject expectedState,
                                                          @NotNull final KafkaQuotaReconciliationConfig options) {

        final ConfigEntryReconciliationConfig configEntryReconciliationConfig = new ConfigEntryReconciliationConfig()
                .withDeleteConfigOrphans(options.isDeleteConfigOrphans());

        List<ConfigEntryChange> configEntryChanges = new ConfigEntryChangeComputer().computeChanges(
                Configs.of(new KafkaQuotaLimitsAdapter(actualState.getConfigs()).toMapDouble()),
                Configs.of(new KafkaQuotaLimitsAdapter(expectedState.getConfigs()).toMapDouble()),
                configEntryReconciliationConfig
        );

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getChange() != ChangeType.NONE);

        var ot = hasChanged ? ChangeType.UPDATE : ChangeType.NONE;

        return new QuotaChange(ot, actualState.getType(), actualState.getEntity(), configEntryChanges);
    }

    private static @NotNull Map<ClientQuotaEntity, QuotaChange> buildChangesForOrphanQuotas(
            @NotNull final Collection<V1KafkaQuotaObject> quotas,
            @NotNull final Set<ClientQuotaEntity> changes) {
        return quotas
                .stream()
                .filter(it -> !changes.contains(newClientQuotaEntity(it.getType(), it.getEntity())))
                .map(quota -> {
                    var adapter = new KafkaQuotaLimitsAdapter(quota.getConfigs());
                    final List<ConfigEntryChange> configChanges = adapter
                            .toMapDouble()
                            .entrySet()
                            .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withBeforeValue(it.getValue())))
                            .collect(Collectors.toList());

                    return new QuotaChange(
                            ChangeType.DELETE,
                            quota.getType(),
                            quota.getEntity(),
                            configChanges

                    );
                })
                .collect(Collectors.toMap(it -> newClientQuotaEntity(it.getType(), it.getEntity()), it -> it));
    }
}
