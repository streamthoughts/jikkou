/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.change.consumer;

import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;

public final class ConsumerGroupChangeComputer extends ResourceChangeComputer<String, V1KafkaConsumerGroup> {

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public ConsumerGroupChangeComputer() {
        super(object -> object.getMetadata().getName(), new ConsumerGroupChangeFactory());
    }

    public static class ConsumerGroupChangeFactory extends ResourceChangeFactory<String, V1KafkaConsumerGroup> {
        @Override
        public ResourceChange createChangeForDelete(String key, V1KafkaConsumerGroup before) {
            return GenericResourceChange.builder(V1KafkaConsumerGroup.class)
                    .withMetadata(before.getMetadata())
                    .withSpec(ResourceChangeSpec.
                            builder()
                            .withOperation(DELETE)
                            .build()
                    )
                    .build();
        }

        @Override
        public ResourceChange createChangeForUpdate(String key, V1KafkaConsumerGroup before, V1KafkaConsumerGroup after) {
            return GenericResourceChange.builder(V1KafkaConsumerGroup.class)
                    .withMetadata(after.getMetadata())
                    .withSpec(ResourceChangeSpec.
                            builder()
                            .withOperation(NONE)
                            .build()
                    )
                    .build();
        }
    }
}
