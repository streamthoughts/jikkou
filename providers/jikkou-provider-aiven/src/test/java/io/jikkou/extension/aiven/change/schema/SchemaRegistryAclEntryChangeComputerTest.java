/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.aiven.change.schema;

import io.jikkou.core.models.CoreAnnotations;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.change.GenericResourceChange;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.models.change.ResourceChangeSpec;
import io.jikkou.core.models.change.StateChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.extension.aiven.MetadataAnnotations;
import io.jikkou.extension.aiven.api.data.Permission;
import io.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchemaRegistryAclEntryChangeComputerTest {
    public static final String TEST_USER = "TestUser";
    public static final String TEST_RESOURCE = "TestSchema";
    public static final String ACL_ENTRY_ID = "1";

    @Test
    void shouldComputeAddChangForNonExistingEntry() {
        // Given
        V1SchemaRegistryAclEntry after = V1SchemaRegistryAclEntry
                .builder()
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withResource(TEST_RESOURCE)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(false);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(after));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create(
                                "entry",
                                new SchemaRegistryAclEntry(Permission.ADMIN.val(), TEST_RESOURCE, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldComputeNoneChangeForExistingEntry() {
        // Given
        V1SchemaRegistryAclEntry entry = V1SchemaRegistryAclEntry
                .builder()
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withResource(TEST_RESOURCE)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(false);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(entry), List.of(entry));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .withChange(StateChange.none(
                                "entry",
                                new SchemaRegistryAclEntry(Permission.ADMIN.val(), TEST_RESOURCE, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldComputeDeleteChangeForEntryWithDeleteAnnotationTrue() {
        // Given
        V1SchemaRegistryAclEntry before = V1SchemaRegistryAclEntry
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID, ACL_ENTRY_ID)
                        .build()
                )
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withResource(TEST_RESOURCE)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();

        V1SchemaRegistryAclEntry after = V1SchemaRegistryAclEntry
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withResource(TEST_RESOURCE)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();

        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(true);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete(
                                "entry",
                                new SchemaRegistryAclEntry(Permission.ADMIN.val(), TEST_RESOURCE, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldComputeNoChangeForEmptyEntryAndDeleteOrphansFalse() {
        // Given
        V1SchemaRegistryAclEntry before = V1SchemaRegistryAclEntry
                .builder()
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withResource(TEST_RESOURCE)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(false);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        Assertions.assertEquals(List.of(), changes);
    }

    @Test
    void shouldComputeDeleteChangeForEmptyEntryAndDeleteOrphansTrue() {
        // Given
        V1SchemaRegistryAclEntry before = V1SchemaRegistryAclEntry
                .builder()
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withResource(TEST_RESOURCE)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        SchemaRegistryAclEntryChangeComputer computer = new SchemaRegistryAclEntryChangeComputer(true);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1SchemaRegistryAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete(
                                "entry",
                                new SchemaRegistryAclEntry(Permission.ADMIN.val(), TEST_RESOURCE, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }
}