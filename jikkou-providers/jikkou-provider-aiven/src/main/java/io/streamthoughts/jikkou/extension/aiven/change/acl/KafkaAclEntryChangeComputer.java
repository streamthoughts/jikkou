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

public final class KafkaAclEntryChangeComputer extends ResourceChangeComputer<KafkaAclEntry, V1KafkaTopicAclEntry, ResourceChange> {


    /**
     * Creates a new {@link KafkaAclEntryChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public KafkaAclEntryChangeComputer(boolean deleteOrphans) {
        super(KafkaAclEntryAdapter::map, new KafkaAclEntryChangeFactory(), deleteOrphans);
    }

    static class KafkaAclEntryChangeFactory extends ResourceChangeFactory<KafkaAclEntry, V1KafkaTopicAclEntry, ResourceChange> {

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
