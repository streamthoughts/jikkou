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
package io.streamthoughts.jikkou.kafka.change;

import static io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs;

import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ConfigEntryChange;
import io.streamthoughts.jikkou.api.change.ConfigEntryChangeComputer;
import io.streamthoughts.jikkou.api.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.Configs;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.jetbrains.annotations.NotNull;

public final class QuotaChangeComputer
        extends ResourceChangeComputer<V1KafkaClientQuota, V1KafkaClientQuota, QuotaChange> {

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
        super(new KeyMapper(), identityChangeValueMapper(), false);
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
     **/
    @Override
    public List<QuotaChange> buildChangeForCreating(V1KafkaClientQuota after) {
        V1KafkaClientQuotaSpec spec = after.getSpec();
        final List<ConfigEntryChange> configChanges = V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs(spec.getConfigs())
                .entrySet()
                .stream().map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withAfterValue(it.getValue())))
                .collect(Collectors.toList());
        return List.of(new QuotaChange(ChangeType.ADD, spec.getType(), spec.getEntity(), configChanges));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<QuotaChange> buildChangeForDeleting(V1KafkaClientQuota before) {
        V1KafkaClientQuotaSpec spec = before.getSpec();
        Map<String, Double> clientQuotaConfigs = toClientQuotaConfigs(spec.getConfigs());
        final List<ConfigEntryChange> configChanges = clientQuotaConfigs
                .entrySet()
                .stream()
                .map(it -> new ConfigEntryChange(it.getKey(), ValueChange.withBeforeValue(it.getValue())))
                .collect(Collectors.toList());

        return List.of(
                new QuotaChange(
                        ChangeType.DELETE,
                        spec.getType(),
                        spec.getEntity(),
                        configChanges
                )
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<QuotaChange> buildChangeForUpdating(V1KafkaClientQuota before, V1KafkaClientQuota after) {
        ConfigEntryChangeComputer configEntryChangeComputer = new ConfigEntryChangeComputer(isLimitDeletionEnabled);

        List<HasMetadataChange<ConfigEntryChange>> configEntryChanges = configEntryChangeComputer.computeChanges(
                Configs.of(toClientQuotaConfigs(before.getSpec().getConfigs())),
                Configs.of(toClientQuotaConfigs(after.getSpec().getConfigs()))
        );

        boolean hasChanged = configEntryChanges.stream()
                .anyMatch(configEntryChange -> configEntryChange.getChange().getChangeType() != ChangeType.NONE);

        var ot = hasChanged ? ChangeType.UPDATE : ChangeType.NONE;

        return List.of(
                new QuotaChange(
                        ot,
                        before.getSpec().getType(),
                        before.getSpec().getEntity(),
                        configEntryChanges.stream().map(HasMetadataChange::getChange).collect(Collectors.toList())
                )
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<QuotaChange> buildChangeForNone(V1KafkaClientQuota before, V1KafkaClientQuota after) {
        return buildChangeForUpdating(before, after);
    }

    static class KeyMapper implements ChangeKeyMapper<V1KafkaClientQuota> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public @NotNull Object apply(@NotNull V1KafkaClientQuota resource) {
            return new ClientQuotaEntity(resource.getSpec().getType().toEntities(resource.getSpec().getEntity()));
        }
    }
}
