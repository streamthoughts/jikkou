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
package io.streamthoughts.jikkou.kafka.validation;

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.kafka.model.KafkaClientQuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaEntity;
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuotaSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
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
    void shouldThrowExceptionGivenNoQuotaType() {
        var resource = V1KafkaClientQuota.builder()
                .withApiVersion(HasMetadata.getApiVersion(V1KafkaTopicList.class))
                .withKind(HasMetadata.getKind(V1KafkaTopicList.class))
                .build();
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            validation.validate(resource);
        });
        exception.printStackTrace();
    }

    @Test
    void shouldThrowExceptionForQuotaTypeClientWithNoEntity() {
        var resource = V1KafkaClientQuota.builder()
                .withApiVersion(HasMetadata.getApiVersion(V1KafkaTopicList.class))
                .withKind(HasMetadata.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity.builder().build())
                        .build())
                .build();
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            validation.validate(resource);
        });
        exception.printStackTrace();
    }

    @Test
    void shouldNotThrowExceptionForQuotaTypeClientWithEntity() {
        var resource = V1KafkaClientQuota.builder()
                .withApiVersion(HasMetadata.getApiVersion(V1KafkaTopicList.class))
                .withKind(HasMetadata.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaClientQuotaSpec
                        .builder()
                        .withType(KafkaClientQuotaType.CLIENT)
                        .withEntity(V1KafkaClientQuotaEntity
                                .builder()
                                .withClientId("client")
                                .build()
                        )
                        .build())
                .build();
        Assertions.assertDoesNotThrow(() -> validation.validate(resource));
    }
}