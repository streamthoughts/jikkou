/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.selector.Selectors;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import io.streamthoughts.jikkou.iceberg.namespace.models.V1IcebergNamespaceSpec;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IcebergNamespaceControllerIT extends BaseExtensionProviderIT {

    @Test
    void shouldCreateNamespace() {
        // GIVEN
        V1IcebergNamespace ns = namespace("analytics", Map.of("owner", "data-team"));

        var context = ReconciliationContext.builder().dryRun(false).build();

        // WHEN
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(ns)), ReconciliationMode.CREATE, context).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.CREATE, results.get(0).change().getSpec().getOp());

        // Verify namespace exists
        ResourceList<V1IcebergNamespace> actual = listNamespaces();
        Assertions.assertTrue(actual.stream()
            .anyMatch(n -> "analytics".equals(n.getMetadata().getName())));
    }

    @Test
    void shouldUpdateNamespaceProperties() {
        // GIVEN - create initial namespace
        V1IcebergNamespace ns = namespace("update_test", Map.of("owner", "team-a"));
        api.reconcile(ResourceList.of(List.of(ns)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - update property
        V1IcebergNamespace updated = namespace("update_test", Map.of("owner", "team-b"));
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(updated)), ReconciliationMode.UPDATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Map<String, ResourceChange> byName = results.stream()
            .map(ChangeResult::change)
            .collect(Collectors.toMap(c -> c.getMetadata().getName(), c -> c));
        Assertions.assertEquals(Operation.UPDATE, byName.get("update_test").getSpec().getOp());
    }

    @Test
    void shouldDeleteNamespace() {
        // GIVEN - create namespace
        V1IcebergNamespace ns = namespace("to_delete", Map.of());
        api.reconcile(ResourceList.of(List.of(ns)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // Verify it exists
        Assertions.assertTrue(listNamespaces().stream()
            .anyMatch(n -> "to_delete".equals(n.getMetadata().getName())));

        // WHEN - delete (must annotate with jikkou.io/delete for DELETE mode)
        V1IcebergNamespaceSpec deleteSpec = new V1IcebergNamespaceSpec();
        V1IcebergNamespace toDelete = V1IcebergNamespace.builder()
            .withMetadata(ObjectMeta.builder()
                .withName("to_delete")
                .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                .build())
            .withSpec(deleteSpec)
            .build();
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(toDelete)), ReconciliationMode.DELETE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.DELETE, results.get(0).change().getSpec().getOp());

        Assertions.assertFalse(listNamespaces().stream()
            .anyMatch(n -> "to_delete".equals(n.getMetadata().getName())));
    }

    @Test
    void shouldProduceNoChangesForIdenticalNamespace() {
        // GIVEN
        V1IcebergNamespace ns = namespace("idempotent", Map.of("owner", "data-team"));
        api.reconcile(ResourceList.of(List.of(ns)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(false).build());

        // WHEN - re-apply same spec
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(ns)), ReconciliationMode.UPDATE,
            ReconciliationContext.builder().dryRun(false).build()).results();

        // THEN
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.NONE, results.get(0).change().getSpec().getOp());
    }

    @Test
    void shouldSupportDryRun() {
        // GIVEN
        V1IcebergNamespace ns = namespace("dry_run_ns", Map.of());

        // WHEN - dry run
        List<ChangeResult> results = api.reconcile(
            ResourceList.of(List.of(ns)), ReconciliationMode.CREATE,
            ReconciliationContext.builder().dryRun(true).build()).results();

        // THEN - should report CREATE but not actually create
        Assertions.assertEquals(1, results.size());
        Assertions.assertEquals(Operation.CREATE, results.get(0).change().getSpec().getOp());

        Assertions.assertFalse(listNamespaces().stream()
            .anyMatch(n -> "dry_run_ns".equals(n.getMetadata().getName())));
    }

    private ResourceList<V1IcebergNamespace> listNamespaces() {
        return api.listResources(V1IcebergNamespace.class, Selectors.NO_SELECTOR, Configuration.empty());
    }

    private static V1IcebergNamespace namespace(String name, Map<String, String> properties) {
        V1IcebergNamespaceSpec spec = new V1IcebergNamespaceSpec();
        spec.setProperties(properties.isEmpty() ? null : properties);
        return V1IcebergNamespace.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(spec)
            .build();
    }
}
