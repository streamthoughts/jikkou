/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KafkaAclEntryAdapterTest {

    public static final String TEST_USER = "TestUser";
    public static final String TEST_TOPIC = "TestTopic";
    public static final String TEST_ACL_ENTRY_ID = "1";

    @Test
    void shouldMapToKafkaAclEntry() {
        // Given
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID, TEST_ACL_ENTRY_ID)
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

        // When
        KafkaAclEntry result = KafkaAclEntryAdapter.map(entry);

        // Then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(TEST_USER, result.username());
        Assertions.assertEquals(TEST_TOPIC, result.topic());
        Assertions.assertEquals(TEST_ACL_ENTRY_ID, result.id());
        Assertions.assertEquals(Permission.ADMIN.val(), result.permission());
    }

    @Test
    void shouldMapToV1KafkaTopicAclEntry() {
        // Given
        KafkaAclEntry entry = new KafkaAclEntry(
                Permission.ADMIN.val(),
                TEST_TOPIC,
                TEST_USER,
                TEST_ACL_ENTRY_ID
        );

        // When
        V1KafkaTopicAclEntry result = KafkaAclEntryAdapter.map(entry);

        // Then
        Assertions.assertNotNull(result);

        V1KafkaTopicAclEntry expected = V1KafkaTopicAclEntry
                .builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withAnnotation(MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID, TEST_ACL_ENTRY_ID)
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

        Assertions.assertEquals(expected, result);
    }
}