/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.change.acl;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import java.util.ArrayList;
import java.util.List;

public final class KafkaAclEntryChangeComputer extends ResourceChangeComputer<KafkaAclEntry, V1KafkaTopicAclEntry> {


    /**
     * Creates a new {@link KafkaAclEntryChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public KafkaAclEntryChangeComputer(boolean deleteOrphans) {
        super(KafkaAclEntryAdapter::map, new KafkaAclEntryChangeFactory(), deleteOrphans);
    }

    static class KafkaAclEntryChangeFactory extends ResourceChangeFactory<KafkaAclEntry, V1KafkaTopicAclEntry> {

        public static final String ENTRY = "entry";

        @Override
        public ResourceChange createChangeForCreate(KafkaAclEntry key, V1KafkaTopicAclEntry after) {
            return createChangeForUpdate(key, null, after);
        }

        @Override
        public ResourceChange createChangeForDelete(KafkaAclEntry key, V1KafkaTopicAclEntry before) {
            return createChangeForUpdate(key, before, null);
        }

        @Override
        public ResourceChange createChangeForUpdate(KafkaAclEntry key,
                                                    V1KafkaTopicAclEntry before,
                                                    V1KafkaTopicAclEntry after) {
            List<StateChange> changes = new ArrayList<>();
            changes.add(StateChange.with(ENTRY,
                    KafkaAclEntryAdapter.map(before),
                    KafkaAclEntryAdapter.map(after))
            );
            return GenericResourceChange
                    .builder(V1KafkaTopicAclEntry.class)
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
