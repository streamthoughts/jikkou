/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.share;

import io.jikkou.core.models.Configs;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.change.ChangeComputerBuilder;
import io.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.jikkou.kafka.change.group.GroupConfigs;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.models.V1KafkaShareGroupSpec;
import java.util.List;
import java.util.Optional;

public final class ShareGroupChangeComputer extends ResourceChangeComputer<String, V1KafkaShareGroup> {

    public ShareGroupChangeComputer(boolean isConfigDeletionEnabled) {
        super(ChangeComputerBuilder.KeyMapper.byName(),
            new ShareGroupChangeFactory(isConfigDeletionEnabled),
            false);
    }

    public static final class ShareGroupChangeFactory extends ResourceChangeFactory<String, V1KafkaShareGroup> {

        private final boolean isConfigDeletionEnabled;

        public ShareGroupChangeFactory(boolean isConfigDeletionEnabled) {
            this.isConfigDeletionEnabled = isConfigDeletionEnabled;
        }

        @Override
        public ResourceChange createChangeForCreate(String key, V1KafkaShareGroup after) {
            return build(after, Operation.CREATE, GroupConfigs.getConfigChanges(null, configs(after), false));
        }

        @Override
        public ResourceChange createChangeForDelete(String key, V1KafkaShareGroup before) {
            return build(before, Operation.DELETE, List.of());
        }

        @Override
        public ResourceChange createChangeForUpdate(String key, V1KafkaShareGroup before, V1KafkaShareGroup after) {
            List<StateChange> configChanges =
                GroupConfigs.getConfigChanges(configs(before), configs(after), isConfigDeletionEnabled);
            boolean changed = configChanges.stream().anyMatch(c -> c.getOp() != Operation.NONE);
            return build(after, changed ? Operation.UPDATE : Operation.NONE, configChanges);
        }

        private static Configs configs(V1KafkaShareGroup group) {
            return Optional.ofNullable(group.getSpec()).map(V1KafkaShareGroupSpec::getConfigs).orElse(null);
        }

        private static ResourceChange build(V1KafkaShareGroup resource, Operation op, List<StateChange> changes) {
            return GenericResourceChange.builder(V1KafkaShareGroup.class)
                .withMetadata(resource.getMetadata())
                .withSpec(ResourceChangeSpec.builder().withOperation(op).withChanges(changes).build())
                .build();
        }
    }
}
