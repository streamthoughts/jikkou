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
package io.streamthoughts.jikkou.extension.aiven.change.quota;

import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaQuotaAdapter;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import java.util.List;

public final class KafkaQuotaChangeComputer extends ResourceChangeComputer<Object, V1KafkaQuota, ResourceChange> {

    /**
     * Creates a new {@link KafkaQuotaChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public KafkaQuotaChangeComputer(boolean deleteOrphans) {
        super(o -> Pair.of(o.getSpec().getUser(), o.getSpec().getClientId()), new V1KafkaQuotaChangeFactory(), deleteOrphans);
    }

    private static class V1KafkaQuotaChangeFactory extends ResourceChangeFactory<Object, V1KafkaQuota, ResourceChange> {

        public static final String ENTRY = "entry";

        @Override
        public ResourceChange createChangeForDelete(Object key, V1KafkaQuota before) {
            return createChangeForUpdate(key, before, null);
        }

        @Override
        public ResourceChange createChangeForCreate(Object key, V1KafkaQuota after) {
            return createChangeForUpdate(key, null, after);
        }

        @Override
        public ResourceChange createChangeForUpdate(Object key, V1KafkaQuota before, V1KafkaQuota after) {
            List<StateChange> changes = List.of(StateChange.with(
                    ENTRY,
                    KafkaQuotaAdapter.map(before),
                    KafkaQuotaAdapter.map(after))
            );
            return GenericResourceChange
                    .builder(V1KafkaQuota.class)
                    .withSpec(ResourceChangeSpec
                            .builder()
                            .withOperation(Change.computeOperation(changes))
                            .withChanges(changes)
                            .build()
                    )
                    .build();
        }
    }
}
