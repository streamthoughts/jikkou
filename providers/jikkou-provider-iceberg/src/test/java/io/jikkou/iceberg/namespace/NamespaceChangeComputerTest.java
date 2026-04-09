/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespaceSpec;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NamespaceChangeComputerTest {

    private static V1IcebergNamespace namespace(String name, Map<String, String> properties) {
        V1IcebergNamespaceSpec spec = new V1IcebergNamespaceSpec();
        spec.setProperties(properties.isEmpty() ? null : properties);

        return V1IcebergNamespace.builder()
            .withMetadata(ObjectMeta.builder().withName(name).build())
            .withSpec(spec)
            .build();
    }

    /**
     * When a namespace exists in the catalog with extra properties (e.g. catalog-managed),
     * re-applying the same user spec (with a subset of properties) should produce NONE.
     */
    @Test
    void shouldNotDetectSpuriousUpdateWhenCatalogInjectsExtraProperties() {
        // "before" = catalog state has extra properties
        V1IcebergNamespace before = namespace("analytics",
            Map.of("owner", "data-team", "catalog.internal.id", "abc123"));

        // "after" = user only specifies their own properties
        V1IcebergNamespace after = namespace("analytics",
            Map.of("owner", "data-team"));

        NamespaceChangeComputer computer = new NamespaceChangeComputer(false);
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.NONE, changes.get(0).getSpec().getOp(),
            "Expected NONE but got " + changes.get(0).getSpec().getOp()
            + " with changes: " + changes.get(0).getSpec().getChanges());
    }

    /**
     * When the user changes a property value, it should be detected as UPDATE.
     */
    @Test
    void shouldDetectPropertyValueChangeAsUpdate() {
        V1IcebergNamespace before = namespace("analytics",
            Map.of("owner", "data-team"));

        V1IcebergNamespace after = namespace("analytics",
            Map.of("owner", "platform-team"));

        NamespaceChangeComputer computer = new NamespaceChangeComputer(false);
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.UPDATE, changes.get(0).getSpec().getOp());
    }
}
