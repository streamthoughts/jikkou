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
package io.streamthoughts.jikkou.extension.aiven.change;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ValueChange;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations;
import io.streamthoughts.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
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
        List<ValueChange<SchemaRegistryAclEntry>> changes = computer.computeChanges(List.of(), List.of(after));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getChangeType());
        Assertions.assertEquals(SchemaRegistryAclEntryAdapter.map(after), changes.get(0).getAfter());
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
        List<ValueChange<SchemaRegistryAclEntry>> changes = computer.computeChanges(List.of(entry), List.of(entry));

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getChangeType());
        Assertions.assertEquals(SchemaRegistryAclEntryAdapter.map(entry), changes.get(0).getBefore());
        Assertions.assertEquals(SchemaRegistryAclEntryAdapter.map(entry), changes.get(0).getAfter());
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
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
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
        List<ValueChange<SchemaRegistryAclEntry>> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        Assertions.assertEquals(1, changes.size());

        ValueChange<SchemaRegistryAclEntry> change = changes.get(0);
        Assertions.assertEquals(ChangeType.DELETE, change.getChangeType());
        Assertions.assertEquals(SchemaRegistryAclEntryAdapter.map(before), change.getBefore());
        Assertions.assertEquals(ACL_ENTRY_ID, change.getBefore().id());
        Assertions.assertNull(change.getAfter());
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
        List<ValueChange<SchemaRegistryAclEntry>> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        Assertions.assertEquals(0, changes.size());
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
        List<ValueChange<SchemaRegistryAclEntry>> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.DELETE, changes.get(0).getChangeType());
        Assertions.assertEquals(SchemaRegistryAclEntryAdapter.map(before), changes.get(0).getBefore());
        Assertions.assertNull(changes.get(0).getAfter());
    }
}