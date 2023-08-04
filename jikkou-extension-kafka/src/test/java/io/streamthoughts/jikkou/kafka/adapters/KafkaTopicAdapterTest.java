/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.kafka.internals.KafkaTopics;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaTopicAdapterTest {

    private final KafkaTopicAdapter adapter = new KafkaTopicAdapter(
            V1KafkaTopic
                    .builder()
                    .withMetadata(ObjectMeta
                            .builder()
                            .withName("test")
                            .build()
                    )
                    .build()
    );

    @Test
    void shouldReturnDefaultPartitionForNull() {
        Assertions.assertEquals(adapter.getPartitionsOrDefault(), KafkaTopics.NO_NUM_PARTITIONS);
    }

    @Test
    void shouldReturnDefaultReplicationFactorForNull() {
        Assertions.assertEquals(adapter.getReplicationFactorOrDefault(), KafkaTopics.NO_REPLICATION_FACTOR);
    }

    @Test
    void shouldReturnDefaultConfigForNull() {
        Assertions.assertEquals(0, adapter.getConfigs().size());
    }
}