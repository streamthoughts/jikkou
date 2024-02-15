/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.change.acl;

import io.streamthoughts.jikkou.core.models.CoreAnnotations;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaAclEntryChangeComputerTest {

    public static final String TEST_USER = "TestUser";
    public static final String TEST_TOPIC = "TestTopic";
    public static final String ACL_ENTRY_ID = "1";

    @Test
    void shouldComputeAddChangForNonExistingEntry() {
        // Given
        V1KafkaTopicAclEntry after = V1KafkaTopicAclEntry
                .builder()
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withTopic(TEST_TOPIC)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(false);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(), List.of(after));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopicAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.CREATE)
                        .withChange(StateChange.create(
                                "entry",
                                new KafkaAclEntry(Permission.ADMIN.val(), TEST_TOPIC, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldComputeNoneChangeForExistingEntry() {
        // Given
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry
                .builder()
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withTopic(TEST_TOPIC)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(false);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(entry), List.of(entry));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopicAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.NONE)
                        .withChange(StateChange.none(
                                "entry",
                                new KafkaAclEntry(Permission.ADMIN.val(), TEST_TOPIC, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldComputeDeleteChangeForEntryWithDeleteAnnotationTrue() {
        // Given
        V1KafkaTopicAclEntry before = V1KafkaTopicAclEntry
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID, ACL_ENTRY_ID)
                        .build()
                )
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withTopic(TEST_TOPIC)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();

        V1KafkaTopicAclEntry after = V1KafkaTopicAclEntry
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(CoreAnnotations.JIKKOU_IO_DELETE, true)
                        .build()
                )
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withTopic(TEST_TOPIC)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();

        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(true);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of(after));

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopicAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete(
                                "entry",
                                new KafkaAclEntry(Permission.ADMIN.val(), TEST_TOPIC, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }

    @Test
    void shouldComputeNoChangeForEmptyEntryAndDeleteOrphansFalse() {
        // Given
        V1KafkaTopicAclEntry before = V1KafkaTopicAclEntry
                .builder()
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withTopic(TEST_TOPIC)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(false);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        Assertions.assertEquals(List.of(), changes);
    }

    @Test
    void shouldComputeDeleteChangeForEmptyEntryAndDeleteOrphansTrue() {
        // Given
        V1KafkaTopicAclEntry before = V1KafkaTopicAclEntry
                .builder()
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(TEST_USER)
                        .withTopic(TEST_TOPIC)
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();
        KafkaAclEntryChangeComputer computer = new KafkaAclEntryChangeComputer(true);

        // When
        List<ResourceChange> changes = computer.computeChanges(List.of(before), List.of());

        // Then
        ResourceChange expected = GenericResourceChange
                .builder(V1KafkaTopicAclEntry.class)
                .withSpec(ResourceChangeSpec
                        .builder()
                        .withOperation(Operation.DELETE)
                        .withChange(StateChange.delete(
                                "entry",
                                new KafkaAclEntry(Permission.ADMIN.val(), TEST_TOPIC, TEST_USER))
                        )
                        .build()
                )
                .build();
        Assertions.assertEquals(List.of(expected), changes);
    }
}