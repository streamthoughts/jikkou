/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.change.consumer;

import io.jikkou.core.models.Configs;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.kafka.models.V1KafkaConsumerGroup;
import io.jikkou.kafka.models.V1KafkaConsumerGroupSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConsumerGroupChangeComputerTest {

    private static V1KafkaConsumerGroup group(String name, Configs configs) {
        return V1KafkaConsumerGroup.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(V1KafkaConsumerGroupSpec.builder().withConfigs(configs).build())
            .build();
    }

    @Test
    void shouldComputeUpdateWhenConfigDrifts() {
        V1KafkaConsumerGroup actual = group("g1", Configs.of("consumer.session.timeout.ms", "45000"));
        V1KafkaConsumerGroup desired = group("g1", Configs.of("consumer.session.timeout.ms", "30000"));
        List<ResourceChange> changes = new ConsumerGroupChangeComputer(false)
            .computeChanges(List.of(actual), List.of(desired));
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }

    @Test
    void shouldComputeNoneWhenConfigMatches() {
        V1KafkaConsumerGroup same = group("g1", Configs.of("consumer.session.timeout.ms", "30000"));
        List<ResourceChange> changes = new ConsumerGroupChangeComputer(false)
            .computeChanges(List.of(same), List.of(same));
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp());
    }
}
