/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.table;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergColumn;
import io.streamthoughts.jikkou.iceberg.table.models.V1IcebergTable;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TableChangeDescriptionTest {

    @Test
    void shouldDescribeCreateOperation() {
        V1IcebergColumn c1 = new V1IcebergColumn();
        c1.setName("id");
        c1.setType("long");
        V1IcebergColumn c2 = new V1IcebergColumn();
        c2.setName("name");
        c2.setType("string");

        ResourceChange change = GenericResourceChange.builder(V1IcebergTable.class)
            .withMetadata(ObjectMeta.builder().withName("ns.my_table").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.CREATE)
                .withChanges(List.of(
                    StateChange.create("schema.columns", List.of(c1, c2)),
                    StateChange.create("property.write.format.default", "parquet")
                ))
                .build())
            .build();

        String description = new TableChangeDescription(change).textual();
        Assertions.assertEquals("Create table 'ns.my_table' (columns=2, format=parquet)", description);
    }

    @Test
    void shouldDescribeDeleteOperation() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergTable.class)
            .withMetadata(ObjectMeta.builder().withName("ns.my_table").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.DELETE)
                .withChange(StateChange.delete("schema.columns", List.of()))
                .build())
            .build();

        String description = new TableChangeDescription(change).textual();
        Assertions.assertEquals("Delete table 'ns.my_table'", description);
    }

    @Test
    void shouldDescribeUpdateWithRenameAddDropAndProperties() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergTable.class)
            .withMetadata(ObjectMeta.builder().withName("ns.my_table").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.UPDATE)
                .withChanges(List.of(
                    StateChange.update("column.old_name.rename", "old_name", "new_name"),
                    StateChange.create("column.email", new V1IcebergColumn()),
                    StateChange.delete("column.legacy", new V1IcebergColumn()),
                    StateChange.update("property.retention", "7d", "30d")
                ))
                .build())
            .build();

        String description = new TableChangeDescription(change).textual();
        Assertions.assertTrue(description.contains("rename=[old_name\u2192new_name]"), description);
        Assertions.assertTrue(description.contains("add=[email]"), description);
        Assertions.assertTrue(description.contains("drop=[legacy]"), description);
        Assertions.assertTrue(description.contains("properties=[retention]"), description);
    }

    @Test
    void shouldDescribeUpdateWithNoDetails() {
        ResourceChange change = GenericResourceChange.builder(V1IcebergTable.class)
            .withMetadata(ObjectMeta.builder().withName("ns.t").build())
            .withSpec(ResourceChangeSpec.builder()
                .withOperation(Operation.UPDATE)
                .withChanges(List.of(
                    StateChange.none("column.id", new V1IcebergColumn())
                ))
                .build())
            .build();

        String description = new TableChangeDescription(change).textual();
        Assertions.assertEquals("Update table 'ns.t'", description);
    }
}
