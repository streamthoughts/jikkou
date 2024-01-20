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
package io.streamthoughts.jikkou.kafka.change.consumer;

import static io.streamthoughts.jikkou.core.reconciler.Operation.DELETE;
import static io.streamthoughts.jikkou.core.reconciler.Operation.NONE;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeComputer;
import io.streamthoughts.jikkou.core.reconciler.change.ResourceChangeFactory;
import io.streamthoughts.jikkou.kafka.models.V1KafkaConsumerGroup;

public final class ConsumerGroupChangeComputer extends ResourceChangeComputer<String, V1KafkaConsumerGroup, ResourceChange> {

    /**
     * Creates a new {@link ResourceChangeComputer} instance.
     */
    public ConsumerGroupChangeComputer() {
        super(object -> object.getMetadata().getName(), new ConsumerGroupChangeFactory());
    }

    public static class ConsumerGroupChangeFactory extends ResourceChangeFactory<String, V1KafkaConsumerGroup, ResourceChange> {
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
