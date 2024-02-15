/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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