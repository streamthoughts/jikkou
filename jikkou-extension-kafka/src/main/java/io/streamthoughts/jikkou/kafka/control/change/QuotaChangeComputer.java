/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.control.change;

import static io.streamthoughts.jikkou.JikkouMetadataAnnotations.isAnnotatedWithDelete;
import static io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs;
import static java.util.stream.Collectors.toMap;

import io.streamthoughts.jikkou.api.control.ChangeComputer;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ConfigEntryChange;
import io.streamthoughts.jikkou.api.control.ConfigEntryChangeComputer;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

public class QuotaChangeComputer implements ChangeComputer<V1KafkaClientQuota, QuotaChange> {

    private boolean isLimitDeletionEnabled;

    /**
     * Creates a new {@link QuotaChangeComputer} instance.
     */
    public QuotaChangeComputer() {
        this(true);
    }

    /**
     * Creates a new {@link TopicChangeComputer} instance.
     *
     * @param isLimitDeletionEnabled {@code true} to delete orphaned limits.
     */
    public QuotaChangeComputer(boolean isLimitDeletionEnabled) {
        isLimitDeletionEnabled(isLimitDeletionEnabled);
    }

    /**
     * Set whether orphaned limits should be deleted or ignored.
     *
     * @param isLimitDeletionEnabled {@code true} to delete, otherwise {@code false}.
     */
    public void isLimitDeletionEnabled(boolean isLimitDeletionEnabled) {
        this.isLimitDeletionEnabled = isLimitDeletionEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QuotaChange> computeChanges(@NotNull final Iterable<V1KafkaClientQuota> actualResources,
                                            @NotNull final Iterable<V1KafkaClientQuota> expectedResources) {


        final Map<ClientQuotaEntity, V1KafkaClientQuota> actualClientQuotasByEntity = StreamSupport
                .stream(actualResources.spliterator(), false)
                .collect(toMap(this::newClientQuotaEntity, it -> it));

        // For Each expected client-quota
        final Map<ClientQuotaEntity, QuotaChange> results = new HashMap<>();
        for (final V1KafkaClientQuota expectedClientQuota : expectedResources) {

            final ClientQuotaEntity key = newClientQuotaEntity(expectedClientQuota);

            // Check whether an existing client-quota is defined
            final V1KafkaClientQuota actualClientQuota = actualClientQuotasByEntity.get(key);

            final QuotaChange change;

            if (isAnnotatedWithDelete(expectedClientQuota)) {
                change = actualClientQuota != null ? buildChangesForOrphanQuotas(expectedClientQuota) : null;
            } else if (actualClientQuota != null) {
                change = buildChangeForExistingQuota(actualClientQuota, expectedClientQuota);
            } else {
                change = buildChangeForNewQuota(expectedClientQuota);
            }

            if (change != null) {
                results.put(key, change);
            }
        }

        return new ArrayList<>(results.values());
    }

    private ClientQuotaEntity newClientQuotaEntity(final V1KafkaClientQuota clientQuota) {
        V1KafkaClientQuotaSpec spec = clientQuota.getSpec();
        return new ClientQuotaEntity(spec.getType().toEntities(spec.getEntity()));
    }

    private QuotaChange buildChangeForNewQuota(final V1KafkaClientQuota quota) {
        V1KafkaClientQuotaSpec spec = quota.getSpec();
        final List<ConfigEntryChange> configChanges = V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs(spec.getConfigs())
                .entrySet()
                .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withAfterValue(it.getValue())))
                .collect(Collectors.toList());
        return new QuotaChange(ChangeType.ADD, spec.getType(), spec.getEntity(), configChanges);
    }


    private QuotaChange buildChangeForExistingQuota(final V1KafkaClientQuota actualResource,
                                                    final V1KafkaClientQuota expectedResource) {

        ConfigEntryChangeComputer configEntryChangeComputer = new ConfigEntryChangeComputer(isLimitDeletionEnabled);

        List<ConfigEntryChange> configEntryChanges = configEntryChangeComputer.computeChanges(
                Configs.of(V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs(actualResource.getSpec().getConfigs())),
                Configs.of(V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs(expectedResource.getSpec().getConfigs()))
        );

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getChangeType() != ChangeType.NONE);

        var ot = hasChanged ? ChangeType.UPDATE : ChangeType.NONE;

        return new QuotaChange(
                ot,
                actualResource.getSpec().getType(),
                actualResource.getSpec().getEntity(),
                configEntryChanges
        );
    }

    private static QuotaChange buildChangesForOrphanQuotas(V1KafkaClientQuota resource) {
        V1KafkaClientQuotaSpec spec = resource.getSpec();
        Map<String, Double> clientQuotaConfigs = toClientQuotaConfigs(spec.getConfigs());
        final List<ConfigEntryChange> configChanges = clientQuotaConfigs
                .entrySet()
                .stream()
                .map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withBeforeValue(it.getValue())))
                .collect(Collectors.toList());

        return new QuotaChange(
                ChangeType.DELETE,
                spec.getType(),
                spec.getEntity(),
                configChanges
        );
    }
}
