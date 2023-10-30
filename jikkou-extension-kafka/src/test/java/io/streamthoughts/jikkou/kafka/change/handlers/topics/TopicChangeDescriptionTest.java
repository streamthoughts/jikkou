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
package io.streamthoughts.jikkou.kafka.change.handlers.topics;

import io.streamthoughts.jikkou.core.models.DefaultResourceChange;
import io.streamthoughts.jikkou.core.reconcilier.ChangeType;
import io.streamthoughts.jikkou.core.reconcilier.change.ConfigEntryChange;
import io.streamthoughts.jikkou.core.reconcilier.change.ValueChange;
import io.streamthoughts.jikkou.kafka.change.TopicChange;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TopicChangeDescriptionTest {

    @Test
    void shouldGetTextualDescription() {
        // Given
        TopicChange change = TopicChange.builder()
                .withName("test")
                .withOperation(ChangeType.ADD)
                .withPartitions(ValueChange.withAfterValue(1))
                .withReplicas(ValueChange.withAfterValue((short) 1))
                .withConfigs(List.of(new ConfigEntryChange("key", ValueChange.withAfterValue("value"))))
                .build();

        // When
        var desc = new TopicChangeDescription(DefaultResourceChange.<TopicChange>builder().withChange(change).build());

        // Then
        Assertions.assertEquals(
                "Add topic 'test' (partitions=1, replicas=1, configs=[key=value])",
                desc.textual()
        );
    }
}