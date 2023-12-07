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
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.DefaultApi;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.ClassExtensionAliasesGenerator;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionFactory;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionRegistry;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceDeserializer;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.AbstractKafkaIntegrationTest;
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

class AdminClientKafkaAclControllerIT extends AbstractKafkaIntegrationTest {

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

        DefaultExtensionRegistry registry = new DefaultExtensionRegistry(
                new DefaultExtensionDescriptorFactory(),
                new ClassExtensionAliasesGenerator()
        );
        api = DefaultApi.builder(new DefaultExtensionFactory(registry), new DefaultResourceRegistry())
                .register(AdminClientKafkaAclController.class, () -> controller)
                .register(AdminClientKafkaAclCollector.class, () -> collector)
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
        ResourceListObject<V1KafkaPrincipalAuthorization> initialResourceList = api
                .listResources(V1KafkaPrincipalAuthorization.class, Selectors.NO_SELECTOR, Configuration.empty());

        ApiChangeResultList result = api.reconcile(resources, ReconciliationMode.CREATE, context);

        ResourceListObject<V1KafkaPrincipalAuthorization> actualResourceList = api
                .listResources(V1KafkaPrincipalAuthorization.class, Selectors.NO_SELECTOR, Configuration.empty());

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
                result.results().size(),
                "Invalid number of changes");

        List<ResourceChange> actual = result.results().stream()
                .map(ChangeResult::change)
                .toList();

        List<ResourceChange> expected = List.of(
                GenericResourceChange.builder(V1KafkaPrincipalAuthorization.class)
                        .withMetadata(new ObjectMeta("User:Alice"))
                        .withSpec(ResourceChangeSpec
                                .builder()
                                .withOperation(Operation.CREATE)
                                .withChange(StateChange.create("acl", new KafkaAclBinding(
                                        "User:Alice",
                                        "my-topic-",
                                        PatternType.PREFIXED,
                                        ResourceType.TOPIC,
                                        AclOperation.READ,
                                        AclPermissionType.ALLOW,
                                        "*",
                                        false
                                )))
                                .build()
                        ).build());
        
        Assertions.assertEquals(expected, actual);
    }
}