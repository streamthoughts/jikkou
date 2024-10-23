/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.reconciler;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.ResourceLoader;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderFactory;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.kafka.BaseExtensionProviderIT;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.util.List;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AdminClientKafkaAclControllerIT extends BaseExtensionProviderIT {

    private final ResourceLoader loader = new ResourceLoader(new ResourceReaderFactory(Jackson.YAML_OBJECT_MAPPER));

    @Test
    public void shouldReconcileKafkaACLsGivenCreateMode() {
        // GIVEN
        var resources = loader.loadFromClasspath("test-kafka-acls.yaml");

        var context = ReconciliationContext.builder()
            .dryRun(false)
            .build();

        // WHEN
        ResourceList<V1KafkaPrincipalAuthorization> initialResourceList = api
            .listResources(V1KafkaPrincipalAuthorization.class, Selectors.NO_SELECTOR, Configuration.empty());

        ApiChangeResultList result = api.reconcile(resources, ReconciliationMode.CREATE, context);

        ResourceList<V1KafkaPrincipalAuthorization> actualResourceList = api
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