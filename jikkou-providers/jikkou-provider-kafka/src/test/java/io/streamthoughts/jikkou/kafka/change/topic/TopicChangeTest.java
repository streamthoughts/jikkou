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