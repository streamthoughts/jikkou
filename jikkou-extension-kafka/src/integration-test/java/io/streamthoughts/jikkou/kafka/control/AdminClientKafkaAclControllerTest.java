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
package io.streamthoughts.jikkou.kafka.control;

import io.streamthoughts.jikkou.api.DefaultApi;
import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.change.Change;
import io.streamthoughts.jikkou.api.change.ChangeResult;
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.ResourceDeserializer;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.kafka.AbstractKafkaIntegrationTest;
import io.streamthoughts.jikkou.kafka.change.AclChange;
import io.streamthoughts.jikkou.kafka.internals.admin.AdminClientContextFactory;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.List;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminClientKafkaAclControllerTest extends AbstractKafkaIntegrationTest {

    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));

    private volatile JikkouApi api;

    @BeforeAll
    public static void beforeAll() {
        ResourceDeserializer.registerKind(V1KafkaPrincipalAuthorization.class);
    }

    @BeforeEach
    public void setUp() {
        AdminClientContextFactory factory = new AdminClientContextFactory(
                Configuration.empty(),
                () -> AdminClient.create(clientConfig())
        );

        var controller = new AdminClientKafkaAclController(factory);
        var collector = new AdminClientKafkaAclCollector(factory);

        api = DefaultApi.builder()
                .withController(controller)
                .withCollector(collector)
                .build();
    }

    @Test
    public void shouldReconcileKafkaACLsGivenCreateMode() {
        // GIVEN
        var resources = loader.loadFromClasspath("test-kafka-acls.yaml");

        var context = ReconciliationContext.builder()
                .dryRun(false)
                .build();

        // WHEN
        List<V1KafkaPrincipalAuthorization> initialResourceList = api
                .getResources(V1KafkaPrincipalAuthorization.class, Configuration.empty());

        List<ChangeResult<Change>> results = api.apply(resources, ReconciliationMode.CREATE, context);

        List<V1KafkaPrincipalAuthorization> actualResourceList = api
                .getResources(V1KafkaPrincipalAuthorization.class, Configuration.empty());

        // THEN
        Assertions.assertEquals(
                0,
                initialResourceList.size(),
                "Invalid number of V1KafkaPrincipalAuthorization [before reconciliation]");
        Assertions.assertEquals(
                1, actualResourceList.size(),
                "Invalid number of V1KafkaPrincipalAuthorization [after reconciliation]");
        Assertions.assertEquals(
                1,
                results.size(),
                "Invalid number of changes");

        List<Change> actual = results.stream()
                .map(ChangeResult::data)
                .map(HasMetadataChange::getChange)
                .toList();

        List<Change> expected = List.of(
                new AclChange(
                        ChangeType.ADD,
                        new KafkaAclBinding(
                                "User:Alice",
                                "my-topic-",
                                PatternType.PREFIXED,
                                ResourceType.TOPIC,
                                AclOperation.READ,
                                AclPermissionType.ALLOW,
                                "*",
                                false
                        )
                )
        );
        Assertions.assertEquals(expected, actual);
    }
}