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
package io.streamthoughts.jikkou.extension.aiven.converter;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntryList;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class V1KafkaAclEntryListConverterTest {

    @Test
    void shouldConvertForV1KafkaAclEntry() {
        // Given
        V1KafkaAclEntryListConverter convert = new V1KafkaAclEntryListConverter();
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry
                .builder()
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername("username")
                        .withTopic("topic")
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();

        // When
        List<V1KafkaTopicAclEntryList> results = convert.convertTo(List.of(entry));

        // Then
        Assertions.assertEquals(1, results.size());
        List<V1KafkaTopicAclEntry> items = results.get(0).getItems();

        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(entry.getSpec(), items.get(0).getSpec());
    }

    @Test
    void shouldConvertForV1KafkaAclEntryList() {
        // Given
        V1KafkaAclEntryListConverter convert = new V1KafkaAclEntryListConverter();
        V1KafkaTopicAclEntry entry = V1KafkaTopicAclEntry
                .builder()
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername("username")
                        .withTopic("topic")
                        .withPermission(Permission.ADMIN)
                        .build()
                )
                .build();

        // When
        List<V1KafkaTopicAclEntry> results = convert.convertFrom(List.of(V1KafkaTopicAclEntryList
                .builder()
                        .withMetadata(ObjectMeta
                                .builder()
                                .withAnnotation("test/annotation", "value")
                                .build())
                        .withItem(entry)
                .build())
        );

        // Then
        Assertions.assertEquals(1, results.size());
        V1KafkaTopicAclEntry item = results.get(0);

        Assertions.assertTrue(item.getMetadata().getAnnotation("test/annotation").isPresent());
        Assertions.assertEquals(entry.getSpec(), item.getSpec());
    }
}