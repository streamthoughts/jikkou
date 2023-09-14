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
package io.streamthoughts.jikkou.kafka.control.handlers.quotas;

import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ConfigEntryChange;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.GenericResourceChange;
import io.streamthoughts.jikkou.kafka.change.QuotaChange;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaEntity;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QuotaChangeDescriptionTest {

    @Test
    void shouldGetTextualDescription() {
        // Given
        QuotaChange change = QuotaChange
                .builder()
                .withType(KafkaClientQuotaType.CLIENT)
                .withOperation(ChangeType.ADD)
                .withEntity(V1KafkaClientQuotaEntity
                        .builder()
                        .withClientId("test-client")
                        .build()
                )
                .withConfigs(List.of(new ConfigEntryChange("producer_byte_rate", ValueChange.withAfterValue("10"))))
                .build();

        // When
        var desc = new QuotaChangeDescription(GenericResourceChange.<QuotaChange>builder().withChange(change).build());

        // Then
        Assertions.assertEquals(
                "Add quotas CLIENT with entity=[client-id=test-client], constraints=[producer_byte_rate=10])",
                desc.textual()
        );
    }

}