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
package io.streamthoughts.jikkou.kafka.change.quota;

import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaClientQuotaTextualDescriptionTest {

    @Test
    void shouldGetTextualDescription() {
        // Given
        ResourceChange change = GenericResourceChange
                .builder()
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withData(KafkaClientQuotaType.CLIENT.toEntities(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("test-client")
                                .build()))
                        .withChange(StateChange.create("producer_byte_rate", 1.0))
                        .build()
                )
                .build();

        // When
        var desc = new KafkaClientQuotaChangeDescription(change);

        // Then
        Assertions.assertEquals(
                "Create quotas for entity={client-id=test-client}, constraints=[producer_byte_rate=1.0])",
                desc.textual()
        );
    }

}