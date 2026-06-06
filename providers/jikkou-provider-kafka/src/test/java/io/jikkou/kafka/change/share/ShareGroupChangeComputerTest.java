/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.share;

import io.jikkou.core.models.Configs;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import io.jikkou.kafka.models.V1KafkaShareGroupSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShareGroupChangeComputerTest {

    private static V1KafkaShareGroup group(String name, Configs configs) {
        return V1KafkaShareGroup.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(V1KafkaShareGroupSpec.builder().withConfigs(configs).build())
            .build();
    }

    @Test
    void shouldComputeCreateWhenGroupAbsent() {
        V1KafkaShareGroup desired = group("g1", Configs.of("share.auto.offset.reset", "earliest"));
        List<ResourceChange> changes = new ShareGroupChangeComputer(false)
            .computeChanges(List.of(), List.of(desired));
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.CREATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldComputeUpdateWhenConfigDrifts() {
        V1KafkaShareGroup actual = group("g1", Configs.of("share.auto.offset.reset", "latest"));
        V1KafkaShareGroup desired = group("g1", Configs.of("share.auto.offset.reset", "earliest"));
        List<ResourceChange> changes = new ShareGroupChangeComputer(false)
            .computeChanges(List.of(actual), List.of(desired));
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldComputeNoneWhenConfigMatches() {
        V1KafkaShareGroup same = group("g1", Configs.of("share.auto.offset.reset", "earliest"));
        List<ResourceChange> changes = new ShareGroupChangeComputer(false)
            .computeChanges(List.of(same), List.of(same));
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp());
    }
}
