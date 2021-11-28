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

import io.streamthoughts.kafka.specs.model.V1QuotaEntityObject;
import io.streamthoughts.kafka.specs.model.V1QuotaObject;
import io.streamthoughts.kafka.specs.model.V1QuotaType;
import io.streamthoughts.kafka.specs.resources.Configs;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuotaChangeComputer implements ChangeComputer<V1QuotaObject, ClientQuotaEntity, QuotaChange, QuotaChangeOptions> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuotaChange> computeChanges(@NotNull final Iterable<V1QuotaObject> actualQuotasObjects,
                                            @NotNull final Iterable<V1QuotaObject> expectedQuotasObjects,
                                            @NotNull final QuotaChangeOptions options) {

        final Map<ClientQuotaEntity, V1QuotaObject> actualQuotasMapByEntity = StreamSupport
                .stream(actualQuotasObjects.spliterator(), false)
                .collect(Collectors.toMap(it -> newClientQuotaEntity(it.type(), it.entity()), o -> o));

        final Map<ClientQuotaEntity, QuotaChange> changes = new HashMap<>();

        for (final V1QuotaObject expectedQuota : expectedQuotasObjects) {

            final ClientQuotaEntity key = newClientQuotaEntity(expectedQuota.type(), expectedQuota.entity());

            final V1QuotaObject actualQuota = actualQuotasMapByEntity.get(key);
            final QuotaChange change = actualQuota == null ?
                    buildChangeForNewQuota(expectedQuota) :
                    buildChangeForExistingQuota(actualQuota, expectedQuota, options);

            changes.put(
                    newClientQuotaEntity(change.getType(), change.getEntity()),
                    change
            );
        }

        if (options.isDeleteQuotaOrphans()) {
            changes.putAll(buildChangesForOrphanQuotas(actualQuotasMapByEntity.values(), changes.keySet()));
        }

        return new ArrayList<>(changes.values());
    }

    @NotNull
    private static ClientQuotaEntity newClientQuotaEntity(final V1QuotaType type,
                                                          final V1QuotaEntityObject entity) {
        return new ClientQuotaEntity(type.toEntities(entity));
    }

    public static QuotaChange buildChangeForNewQuota(@NotNull final V1QuotaObject quota) {
        final List<ConfigEntryChange> configChanges = quota.configs()
                .toMapDouble()
                .entrySet()
                .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withAfterValue(it.getValue())))
                .collect(Collectors.toList());
        return new QuotaChange(Change.OperationType.ADD, quota.type(), quota.entity(), configChanges);
    }


    public static QuotaChange buildChangeForExistingQuota(@NotNull final V1QuotaObject actualState,
                                                          @NotNull final V1QuotaObject expectedState,
                                                          @NotNull final QuotaChangeOptions options) {


        final ConfigEntryOptions configEntryOptions = new ConfigEntryOptions()
                .withDeleteConfigOrphans(options.isDeleteConfigOrphans());

        List<ConfigEntryChange> configEntryChanges = new ConfigEntryChangeComputer().computeChanges(
                Configs.of(actualState.configs().toMapDouble()),
                Configs.of(expectedState.configs().toMapDouble()),
                configEntryOptions
        );

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getOperation() != Change.OperationType.NONE);

        var ot = hasChanged ? Change.OperationType.UPDATE : Change.OperationType.NONE;

        return new QuotaChange(ot, actualState.type(), actualState.entity(), configEntryChanges);
    }

    private static @NotNull Map<ClientQuotaEntity, QuotaChange> buildChangesForOrphanQuotas(
            @NotNull final Collection<V1QuotaObject> quotas,
            @NotNull final Set<ClientQuotaEntity> changes) {
        return quotas
                .stream()
                .filter(it -> !changes.contains(newClientQuotaEntity(it.type(), it.entity())))
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
                .collect(Collectors.toMap(it -> newClientQuotaEntity(it.getType(), it.getEntity()), it -> it));
    }
}
