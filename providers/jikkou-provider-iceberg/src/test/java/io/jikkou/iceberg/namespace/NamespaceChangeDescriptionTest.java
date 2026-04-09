/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.namespace;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.iceberg.namespace.models.V1IcebergNamespace;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NamespaceChangeDescriptionTest {

    @Test
    void shouldDescribeCreateOperation() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergNamespace.class)
            .withMetadata(ObjectMeta.builder().withName("analytics").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create("property.owner", "data-team"))
                .build())
            .build();

        String description = new NamespaceChangeDescription(change).textual();
        Assertions.assertEquals("Create namespace 'analytics'", description);
    }

    @Test
    void shouldDescribeDeleteOperation() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergNamespace.class)
            .withMetadata(ObjectMeta.builder().withName("analytics").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.DELETE)
                .withChange(StateChange.delete("property.owner", "data-team"))
                .build())
            .build();

        String description = new NamespaceChangeDescription(change).textual();
        Assertions.assertEquals("Delete namespace 'analytics'", description);
    }

    @Test
    void shouldDescribeUpdateOperationWithChangedProperties() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergNamespace.class)
            .withMetadata(ObjectMeta.builder().withName("analytics").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.UPDATE)
                .withChanges(List.of(
                    StateChange.update("property.owner", "data-team", "platform-team"),
                    StateChange.create("property.retention", "30d"),
                    StateChange.none("property.env", "prod")
                ))
                .build())
            .build();

        String description = new NamespaceChangeDescription(change).textual();
        Assertions.assertEquals("Update namespace 'analytics' (set=[owner, retention])", description);
    }

    @Test
    void shouldDescribeUpdateOperationWithNoPropertyChanges() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergNamespace.class)
            .withMetadata(ObjectMeta.builder().withName("analytics").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.UPDATE)
                .withChanges(List.of(
                    StateChange.none("property.owner", "data-team")
                ))
                .build())
            .build();

        String description = new NamespaceChangeDescription(change).textual();
        Assertions.assertEquals("Update namespace 'analytics'", description);
    }
}
