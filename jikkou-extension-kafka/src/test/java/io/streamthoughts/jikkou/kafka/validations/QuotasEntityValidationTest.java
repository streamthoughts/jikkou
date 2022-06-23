/*
 * Copyright 2022 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.validations;

import io.streamthoughts.jikkou.api.error.ValidationException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.kafka.model.QuotaType;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaObject;
import io.streamthoughts.jikkou.kafka.models.V1KafkaQuotaSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopicList;
import io.streamthoughts.jikkou.kafka.models.V1QuotaEntityObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuotasEntityValidationTest {

    private QuotasEntityValidation validation;

    @BeforeEach
    public void before() {
        validation = new QuotasEntityValidation();
    }

    @Test
    void should_throw_exception_given_no_quota_type() {
        var resource = V1KafkaQuotaList.builder()
                .withApiVersion(HasMetadata.getApiVersion(V1KafkaTopicList.class))
                .withKind(HasMetadata.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withQuota(V1KafkaQuotaObject
                                .builder()
                                .build()
                        )
                        .build())
                .build();
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            validation.validate(resource);
        });
        exception.printStackTrace();
    }

    @Test
    void should_throw_exception_given_quota_type_client_with_no_entity() {
        var resource = V1KafkaQuotaList.builder()
                .withApiVersion(HasMetadata.getApiVersion(V1KafkaTopicList.class))
                .withKind(HasMetadata.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withQuota(V1KafkaQuotaObject
                                .builder()
                                .withType(QuotaType.CLIENT)
                                .withEntity(V1QuotaEntityObject.
                                        builder()
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .build();
        ValidationException exception = Assertions.assertThrows(ValidationException.class, () -> {
            validation.validate(resource);
        });
        exception.printStackTrace();
    }

    @Test
    void should_throw_exception_given_quota_type_client_with_entity() {
        var resource = V1KafkaQuotaList.builder()
                .withApiVersion(HasMetadata.getApiVersion(V1KafkaTopicList.class))
                .withKind(HasMetadata.getKind(V1KafkaTopicList.class))
                .withSpec(V1KafkaQuotaSpec
                        .builder()
                        .withQuota(V1KafkaQuotaObject
                                .builder()
                                .withType(QuotaType.CLIENT)
                                .withEntity(V1QuotaEntityObject.
                                        builder()
                                        .withClientId("client")
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .build();
        Assertions.assertDoesNotThrow(() -> validation.validate(resource));
    }
}