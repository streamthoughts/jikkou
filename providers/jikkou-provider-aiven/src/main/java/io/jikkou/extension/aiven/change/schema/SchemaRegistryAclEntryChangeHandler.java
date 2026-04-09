/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.change.schema;

import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.ChangeHandler;
import io.jikkou.core.reconciler.ChangeResponse;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import io.jikkou.extension.aiven.api.AivenApiClient;
import io.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.jikkou.extension.aiven.change.AbstractChangeHandler;
import io.jikkou.extension.aiven.change.KafkaChangeDescriptions;
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
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
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
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.deleteSchemaRegistryAclEntry(getEntry(change, SchemaRegistryAclEntry.class).id()))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None {
        public None() {
            super(change -> KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, SchemaRegistryAclEntry.class)));
        }
    }
}
