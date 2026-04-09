/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.change;

import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Change;
import io.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.jikkou.extension.confluent.adapter.RoleBindingAdapter;
import io.jikkou.extension.confluent.api.data.RoleBindingData;
import io.jikkou.extension.confluent.models.V1RoleBinding;
import java.util.ArrayList;
import java.util.List;

public final class RoleBindingChangeComputer extends ResourceChangeComputer<RoleBindingData, V1RoleBinding> {

    /**
     * Creates a new {@link RoleBindingChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphan entries must be deleted.
     */
    public RoleBindingChangeComputer(boolean deleteOrphans) {
        super(RoleBindingAdapter::map, new RoleBindingChangeFactory(), deleteOrphans);
    }

    static class RoleBindingChangeFactory extends ResourceChangeFactory<RoleBindingData, V1RoleBinding> {

        public static final String ENTRY = "entry";

        @Override
        public ResourceChange createChangeForCreate(RoleBindingData key, V1RoleBinding after) {
            return createChangeForUpdate(key, null, after);
        }

        @Override
        public ResourceChange createChangeForDelete(RoleBindingData key, V1RoleBinding before) {
            return createChangeForUpdate(key, before, null);
        }

        @Override
        public ResourceChange createChangeForUpdate(RoleBindingData key,
                                                    V1RoleBinding before,
                                                    V1RoleBinding after) {
            List<StateChange> changes = new ArrayList<>();
            changes.add(StateChange.with(ENTRY,
                RoleBindingAdapter.map(before),
                RoleBindingAdapter.map(after))
            );
            return GenericResourceChange
                .builder(V1RoleBinding.class)
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
