/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.topic;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.change.topics.TopicChange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopicChangeTest {

    @Test
    void shouldGetTextualDescription() {
        // Given
        ResourceChange change = GenericResourceChange.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName("test")
                        .build()
                )
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create("partitions", 1))
                        .withChange(StateChange.create("replicas", (short)1))
                        .withChange(StateChange.create("config.key", "value"))
                        .build()
                )
                .build();

        // When
        var desc = TopicChange.getDescription(change);

        // Then
        Assertions.assertEquals(
                "Create topic 'test' (partitions=1, replicas=1, configs=[key=value])",
                desc.textual()
        );
    }
}