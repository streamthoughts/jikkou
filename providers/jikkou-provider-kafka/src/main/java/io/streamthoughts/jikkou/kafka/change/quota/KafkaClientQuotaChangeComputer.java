/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.kafka.change.quota;

import static io.streamthoughts.jikkou.kafka.adapters.V1KafkaClientQuotaConfigsAdapter.toClientQuotaConfigs;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.change.ChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChangeComputer;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.quota.ClientQuotaEntity;

public final class KafkaClientQuotaChangeComputer extends ResourceChangeComputer<ClientQuotaEntity, V1KafkaClientQuota, ResourceChange> {

    /**
     * Creates a new {@link TopicChangeComputer} instance.
     *
     * @param isLimitDeletionEnabled {@code true} to delete orphaned limits.
     */
    public KafkaClientQuotaChangeComputer(boolean isLimitDeletionEnabled) {
        super(
                object -> new ClientQuotaEntity(object.getSpec().getType().toEntities(object.getSpec().getEntity())),
                new KafkaClientQuotaChangeFactory(isLimitDeletionEnabled)
        );
    }

    public static final class KafkaClientQuotaChangeFactory extends ResourceChangeFactory<ClientQuotaEntity, V1KafkaClientQuota, ResourceChange> {

        private final boolean isLimitDeletionEnabled;

        public KafkaClientQuotaChangeFactory(boolean isLimitDeletionEnabled) {
            this.isLimitDeletionEnabled = isLimitDeletionEnabled;
        }

        @Override
        public ResourceChange createChangeForCreate(ClientQuotaEntity key, V1KafkaClientQuota after) {
            List<StateChange> changes = ChangeComputer.computeChanges(
                    Collections.emptyMap(),
                    toClientQuotaConfigs(after.getSpec().getConfigs()),
                    isLimitDeletionEnabled
            );
            return buildChange(Operation.CREATE, after, changes);
        }

        @Override
        public ResourceChange createChangeForDelete(ClientQuotaEntity key,
                                                    V1KafkaClientQuota before) {
            List<StateChange> changes = ChangeComputer.computeChanges(
                    toClientQuotaConfigs(before.getSpec().getConfigs()),
                    Collections.emptyMap(),
                    isLimitDeletionEnabled
            );

            return buildChange(Operation.DELETE, before, changes);
        }

        @Override
        public ResourceChange createChangeForUpdate(ClientQuotaEntity key,
                                                    V1KafkaClientQuota before,
                                                    V1KafkaClientQuota after) {
            List<StateChange> changes = ChangeComputer.computeChanges(
                    toClientQuotaConfigs(before.getSpec().getConfigs()),
                    toClientQuotaConfigs(after.getSpec().getConfigs()),
                    isLimitDeletionEnabled
            );

            Operation operation = Change.computeOperation(changes);
            return buildChange(operation, before, changes);
        }

        private static ResourceChange buildChange(Operation op,
                                                  V1KafkaClientQuota object,
                                                  List<StateChange> changes) {

            KafkaClientQuotaEntity entity = object.getSpec().getEntity();
            Map<String, String> entities = object
                    .getSpec()
                    .getType()
                    .toEntities(entity);
            return GenericResourceChange
                    .builder(V1KafkaClientQuota.class)
                    .withMetadata(object.getMetadata())
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(op)
                            .withData(entities)
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }
    }
}
