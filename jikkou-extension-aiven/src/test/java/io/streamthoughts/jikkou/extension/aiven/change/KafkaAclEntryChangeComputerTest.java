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
import io.streamthoughts.jikkou.api.change.ChangeType;
import io.streamthoughts.jikkou.api.change.ValueChange;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations;
import io.streamthoughts.jikkou.extension.aiven.adapter.KafkaAclEntryAdapter;
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
        List<ValueChange<KafkaAclEntry>> changes = computer.computeChanges(List.of(), List.of(after))
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.ADD, changes.get(0).getChangeType());
        Assertions.assertEquals(KafkaAclEntryAdapter.map(after), changes.get(0).getAfter());
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
        List<ValueChange<KafkaAclEntry>> changes = computer.computeChanges(List.of(entry), List.of(entry))
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.NONE, changes.get(0).getChangeType());
        Assertions.assertEquals(KafkaAclEntryAdapter.map(entry), changes.get(0).getBefore());
        Assertions.assertEquals(KafkaAclEntryAdapter.map(entry), changes.get(0).getAfter());
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
                        .withAnnotation(JikkouMetadataAnnotations.JIKKOU_IO_DELETE, true)
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
        List<ValueChange<KafkaAclEntry>> changes = computer.computeChanges(List.of(before), List.of(after))
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());

        ValueChange<KafkaAclEntry> change = changes.get(0);
        Assertions.assertEquals(ChangeType.DELETE, change.getChangeType());
        Assertions.assertEquals(KafkaAclEntryAdapter.map(before), change.getBefore());
        Assertions.assertEquals(ACL_ENTRY_ID, change.getBefore().id());
        Assertions.assertNull(change.getAfter());
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
        List<ValueChange<KafkaAclEntry>> changes = computer.computeChanges(List.of(before), List.of())
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(0, changes.size());
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
        List<ValueChange<KafkaAclEntry>> changes = computer.computeChanges(List.of(before), List.of())
                .stream().map(HasMetadataChange::getChange).toList();

        // Then
        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(ChangeType.DELETE, changes.get(0).getChangeType());
        Assertions.assertEquals(KafkaAclEntryAdapter.map(before), changes.get(0).getBefore());
        Assertions.assertNull(changes.get(0).getAfter());
    }
}