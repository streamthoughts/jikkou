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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.validation.ValidationResult;
import io.streamthoughts.jikkou.kafka.collections.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientQuotaValidationTest {

    private ClientQuotaValidation validation;

    @BeforeEach
    public void before() {
        validation = new ClientQuotaValidation();
    }

    @Test
    void shouldReturnErrorForNoQuotaType() {
        // Given
        var resource = V1KafkaClientQuota.builder()
                .withApiVersion(Resource.getApiVersion(V1KafkaTopicList.class))
                .withKind(Resource.getKind(V1KafkaTopicList.class))
                .build();
        // When
        ValidationResult result = validation.validate(resource);

        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldReturnErrorForQuotaTypeClientWithNoEntity() {
        // Given
        var resource = V1KafkaClientQuota.builder()
                .withApiVersion(Resource.getApiVersion(V1KafkaTopicList.class))
                .withKind(Resource.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity.builder().build())
                        .build())
                .build();
        // When
        ValidationResult result = validation.validate(resource);
        // Then
        Assertions.assertFalse(result.isValid());
    }

    @Test
    void shouldNotReturnErrorForQuotaTypeClientWithEntity() {
        // Given
        var resource = V1KafkaClientQuota.builder()
                .withApiVersion(Resource.getApiVersion(V1KafkaTopicList.class))
                .withKind(Resource.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(KafkaClientQuotaEntity
                                .builder()
                                .withClientId("client")
                                .build()
                        )
                        .build())
                .build();
        // When
        ValidationResult result = validation.validate(resource);
        // Then
        Assertions.assertTrue(result.isValid());
    }
}