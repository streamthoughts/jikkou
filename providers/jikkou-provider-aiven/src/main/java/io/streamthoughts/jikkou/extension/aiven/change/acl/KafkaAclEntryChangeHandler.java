/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.change.acl;

import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeHandler;
import io.streamthoughts.jikkou.core.reconciler.ChangeResponse;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import io.streamthoughts.jikkou.extension.aiven.api.AivenApiClient;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.change.AbstractChangeHandler;
import io.streamthoughts.jikkou.extension.aiven.change.KafkaChangeDescriptions;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public abstract class KafkaAclEntryChangeHandler extends AbstractChangeHandler {

    public KafkaAclEntryChangeHandler(@NotNull final AivenApiClient api,
                                      @NotNull final Operation supportedOperation) {
        super(api, Set.of(supportedOperation));
    }

    public static class Create extends KafkaAclEntryChangeHandler {

        public Create(@NotNull final AivenApiClient api) {
            super(api, Operation.CREATE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.addKafkaAclEntry(getEntry(change, KafkaAclEntry.class)))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class Delete extends KafkaAclEntryChangeHandler {

        public Delete(@NotNull final AivenApiClient api) {
            super(api, Operation.DELETE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<ChangeResponse> handleChanges(@NotNull List<ResourceChange> changes) {
            return changes.stream()
                    .map(change -> executeAsync(
                            change,
                            () -> api.deleteKafkaAclEntry(getEntry(change, KafkaAclEntry.class).id()))
                    )
                    .collect(Collectors.toList());
        }
    }

    public static class None extends ChangeHandler.None {
        public None() {
            super(change -> KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, KafkaAclEntry.class)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextDescription describe(@NotNull ResourceChange change) {
        return KafkaChangeDescriptions.of(change.getSpec().getOp(), getEntry(change, KafkaAclEntry.class));
    }
}
