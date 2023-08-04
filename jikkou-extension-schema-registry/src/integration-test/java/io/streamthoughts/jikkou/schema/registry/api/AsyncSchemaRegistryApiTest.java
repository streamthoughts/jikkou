/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.schema.registry.api;

import static io.streamthoughts.jikkou.schema.registry.AbstractIntegrationTest.CONFLUENT_PLATFORM_VERSION;

import io.streamthoughts.jikkou.rest.client.RestClientBuilder;
import io.streamthoughts.jikkou.rest.client.RestClientException;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityCheck;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityLevelObject;
import io.streamthoughts.jikkou.schema.registry.api.data.CompatibilityObject;
import io.streamthoughts.jikkou.schema.registry.api.data.ErrorResponse;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaId;
import io.streamthoughts.jikkou.schema.registry.api.data.SubjectSchemaRegistration;
import io.streamthoughts.jikkou.schema.registry.model.CompatibilityLevels;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AsyncSchemaRegistryApiTest {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncSchemaRegistryApiTest.class);

    private static final Network KAFKA_NETWORK = Network.newNetwork();

    @Container
    private static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag(CONFLUENT_PLATFORM_VERSION)).withKraft()
            .withNetwork(KAFKA_NETWORK)
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withLogConsumer(new Slf4jLogConsumer(LOG));

    @Container
    private static final SchemaRegistryContainer SCHEMA_REGISTRY =
            new SchemaRegistryContainer(CONFLUENT_PLATFORM_VERSION)
                    .withNetwork(KAFKA_NETWORK)
                    .withKafka(KAFKA_CONTAINER)
                    .withLogConsumer(new Slf4jLogConsumer(LOG));
    public static final String TEST_SUBJECT = "test";

    private static final String AVRO_SCHEMA = """
            {
              "namespace": "example.avro",
              "type": "record",
              "name": "User",
              "fields": [
                 {"name": "name", "type": "string"},
                 {"name": "favorite_number",  "type": ["int", "null"]}
              ]
            }
            """;

    private static final String AVRO_SCHEMA_NOT_COMPATIBLE = """
            {
              "namespace": "example.avro",
              "type": "record",
              "name": "User",
              "fields": [
                 {"name": "name", "type": "string"},
                 {"name": "favorite_number",  "type": ["string", "null"]}
              ]
            }
            """;

    private AsyncSchemaRegistryApi async;

    @BeforeEach
    public void beforeEach() {
        SchemaRegistryApi api = RestClientBuilder
                .newBuilder()
                .baseUri(SCHEMA_REGISTRY.getSchemaRegistryUrl())
                .enableClientDebugging(true)
                .build(SchemaRegistryApi.class);
        async = new AsyncSchemaRegistryApi(api);
    }

    @Order(1)
    @Test
    void shouldGetGlobalCompatibilityLevel() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<CompatibilityLevelObject> future = async.getGlobalCompatibility();

        // Then
        CompatibilityLevelObject result = future.get();
        Assertions.assertEquals("BACKWARD", result.compatibilityLevel());
    }

    @Order(2)
    @Test
    void shouldListSchemaForEmptySubject() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<List<String>> future = async.listSubjects();

        // Then
        List<String> results = future.get();
        Assertions.assertTrue(results.isEmpty());
    }

    @Order(3)
    @Test
    void shouldRegisterSchemaVersionForNewSubject() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<SubjectSchemaId> future = async.registerSubjectVersion(
                TEST_SUBJECT,
                new SubjectSchemaRegistration(AVRO_SCHEMA, SchemaType.AVRO),
                true
        );

        // Then
        SubjectSchemaId result = future.get();
        Assertions.assertEquals(1, result.id());
    }

    @Order(4)
    @Test
    void shouldListSchemaForExistingSubject() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<List<String>> future = async.listSubjects();

        // Then
        List<String> results = future.get();
        Assertions.assertFalse(results.isEmpty());
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(TEST_SUBJECT, results.get(0));
    }

    @Order(5)
    @Test
    void shouldUpdateCompatibilityForExistingSubject() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<CompatibilityObject> future = async.updateConfigCompatibility(
                TEST_SUBJECT,
                new CompatibilityObject(CompatibilityLevels.FULL_TRANSITIVE.name())
        );

        // Then
        CompatibilityObject result = future.get();
        Assertions.assertEquals(CompatibilityLevels.FULL_TRANSITIVE.name(), result.compatibility());
    }

    @Order(6)
    @Test
    void shouldGetCompatibilityForExistingSubject() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<CompatibilityLevelObject> future = async.getConfigCompatibility(TEST_SUBJECT, false);

        // Then
        CompatibilityLevelObject result = future.get();
        Assertions.assertEquals(CompatibilityLevels.FULL_TRANSITIVE.name(), result.compatibilityLevel());
    }

    @Order(6)
    @Test
    void shouldGetErrorCompatibilityForNotExistingSubjectAndDefaultToGlobalFalse() {
        // When
        CompletableFuture<CompatibilityLevelObject> future = async.getConfigCompatibility("unknown", false);

        // Then
        RestClientException exception = Assertions
                .assertThrowsExactly(RestClientException.class, () -> {
                    try {
                        future.get();
                    } catch (ExecutionException e) {
                        throw e.getCause();
                    }
                });

        ErrorResponse response =  exception.getResponseEntity(ErrorResponse.class);
        Assertions.assertEquals(40408, response.errorCode());
        Assertions.assertEquals("Subject 'unknown' does not have subject-level compatibility configured", response.message());
    }

    @Order(7)
    @Test
    void shouldGetGlobalCompatibilityForNotExistingSubjectAndDefaultToGlobalTrue() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<CompatibilityLevelObject> future = async.getConfigCompatibility("unknown", true);

        // Then
        CompatibilityLevelObject result = future.get();
        Assertions.assertEquals(CompatibilityLevels.BACKWARD.name(), result.compatibilityLevel());
    }

    @Order(8)
    @Test
    void shouldGetTrueForTestingCompatibleSchema() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<CompatibilityCheck> future = async.testCompatibility(
                TEST_SUBJECT,
                -1,
                true,
                new SubjectSchemaRegistration(AVRO_SCHEMA, SchemaType.AVRO)
        );

        // Then
        CompatibilityCheck result = future.get();
        Assertions.assertTrue(result.isCompatible());
        Assertions.assertTrue(result.getMessages().isEmpty());
    }

    @Order(9)
    @Test
    void shouldGetFalseForTestingCompatibleSchema() throws ExecutionException, InterruptedException {
        // When
        CompletableFuture<CompatibilityCheck> future = async.testCompatibility(
                TEST_SUBJECT,
                -1,
                true,
                new SubjectSchemaRegistration(AVRO_SCHEMA_NOT_COMPATIBLE, SchemaType.AVRO)
        );

        // Then
        CompatibilityCheck result = future.get();
        Assertions.assertFalse(result.isCompatible());
        Assertions.assertFalse(result.getMessages().isEmpty());
    }
}