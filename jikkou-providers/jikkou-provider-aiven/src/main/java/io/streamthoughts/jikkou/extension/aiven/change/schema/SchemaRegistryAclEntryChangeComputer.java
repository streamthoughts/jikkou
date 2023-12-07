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
package io.streamthoughts.jikkou.extension.aiven.change.schema;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.util.ArrayList;
import java.util.List;

public final class SchemaRegistryAclEntryChangeComputer
        extends ResourceChangeComputer<SchemaRegistryAclEntry, V1SchemaRegistryAclEntry, ResourceChange> {

    /**
     * Creates a new {@link SchemaRegistryAclEntryChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public SchemaRegistryAclEntryChangeComputer(boolean deleteOrphans) {
        super(SchemaRegistryAclEntryAdapter::map, new SchemaRegistryAclEntryChangeFactory(), deleteOrphans);
    }

    public static class SchemaRegistryAclEntryChangeFactory extends ResourceChangeFactory<SchemaRegistryAclEntry, V1SchemaRegistryAclEntry, ResourceChange> {
        @Override
        public ResourceChange createChangeForCreate(SchemaRegistryAclEntry key, V1SchemaRegistryAclEntry after) {
            return createChangeForUpdate(key, null, after);
        }

        @Override
        public ResourceChange createChangeForDelete(SchemaRegistryAclEntry key, V1SchemaRegistryAclEntry before) {
            return createChangeForUpdate(key, before, null);
        }

        @Override
        public ResourceChange createChangeForUpdate(SchemaRegistryAclEntry key, V1SchemaRegistryAclEntry before, V1SchemaRegistryAclEntry after) {
            List<StateChange> changes = new ArrayList<>();
            changes.add(StateChange.with("entry",
                    SchemaRegistryAclEntryAdapter.map(before),
                    SchemaRegistryAclEntryAdapter.map(after))
            );

            return GenericResourceChange
                    .builder(V1SchemaRegistryAclEntry.class)
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
