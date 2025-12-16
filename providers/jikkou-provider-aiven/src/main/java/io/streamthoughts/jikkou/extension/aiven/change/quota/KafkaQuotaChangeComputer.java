/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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

public final class KafkaQuotaChangeComputer extends ResourceChangeComputer<Object, V1KafkaQuota> {

    /**
     * Creates a new {@link KafkaQuotaChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public KafkaQuotaChangeComputer(boolean deleteOrphans) {
        super(o -> Pair.of(o.getSpec().getUser(), o.getSpec().getClientId()), new V1KafkaQuotaChangeFactory(), deleteOrphans);
    }

    private static class V1KafkaQuotaChangeFactory extends ResourceChangeFactory<Object, V1KafkaQuota> {

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
