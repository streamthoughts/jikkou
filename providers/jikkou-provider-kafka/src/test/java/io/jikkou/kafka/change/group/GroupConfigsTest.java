/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.group;

import io.jikkou.core.models.Configs;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GroupConfigsTest {

    @Test
    void shouldComputeSetChangeForNewConfig() {
        Configs after = Configs.of("share.auto.offset.reset", "earliest");
        List<StateChange> changes = GroupConfigs.getConfigChanges(null, after, false);
        Assertions.assertEquals(1, changes.size());
        StateChange change = changes.get(0);
        Assertions.assertEquals("config.share.auto.offset.reset", change.getName());
        Assertions.assertEquals(Operation.CREATE, change.getOp());
        Assertions.assertEquals("earliest", change.getAfter());
    }

    @Test
    void shouldComputeUpdateChangeForChangedConfig() {
        Configs before = Configs.of("share.record.lock.duration.ms", "15000");
        Configs after = Configs.of("share.record.lock.duration.ms", "30000");
        List<StateChange> changes = GroupConfigs.getConfigChanges(before, after, false);
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getOp());
    }

    @Test
    void shouldNotDeleteOrphanWhenDeletionDisabled() {
        Configs before = Configs.of("share.auto.offset.reset", "earliest");
        List<StateChange> changes = GroupConfigs.getConfigChanges(before, null, false);
        Assertions.assertTrue(changes.stream().noneMatch(c -> c.getOp() == Operation.DELETE));
    }

    @Test
    void shouldDeleteOrphanWhenDeletionEnabled() {
        Configs before = Configs.of("share.auto.offset.reset", "earliest");
        List<StateChange> changes = GroupConfigs.getConfigChanges(before, null, true);
        Assertions.assertEquals(Operation.DELETE, changes.get(0).getOp());
    }
}
