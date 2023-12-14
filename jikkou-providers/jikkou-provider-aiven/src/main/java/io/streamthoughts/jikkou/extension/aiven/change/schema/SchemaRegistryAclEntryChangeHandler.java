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

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.change.AbstractChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class SchemaRegistryAclEntryChangeHandler extends AbstractChangeHandler {

    public SchemaRegistryAclEntryChangeHandler(@NotNull final AivenApiClient api,
                                               @NotNull final Operation supportedOperation) {
        super(api, Set.of(supportedOperation));
    }

    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, SchemaRegistryAclEntry.class));
    }

    public static class Create extends SchemaRegistryAclEntryChangeHandler {


        public Create(@NotNull AivenApiClient api) {
            super(api, Operation.CREATE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.addSchemaRegistryAclEntry(getEntry(change, SchemaRegistryAclEntry.class)))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class Delete extends SchemaRegistryAclEntryChangeHandler {
        public Delete(@NotNull AivenApiClient api) {
            super(api, Operation.DELETE);
        }

        @Override
        public List<ChangeResponse<ResourceChange>> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.deleteSchemaRegistryAclEntry(getEntry(change, SchemaRegistryAclEntry.class).id()))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None<ResourceChange> {
        public None() {
            super(change -> KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, SchemaRegistryAclEntry.class)));
        }
    }
}
