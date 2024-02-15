/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import static io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.api.data.KafkaAclEntry;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntrySpec;
import java.util.List;
import java.util.Locale;

public final class KafkaAclEntryAdapter {

    public static final String NO_ENTRY_ID = null;

    public static KafkaAclEntry map(final V1KafkaTopicAclEntry entry, String id) {
        if (entry == null) return null;
        V1KafkaTopicAclEntrySpec spec = entry.getSpec();
        if (id == null) {
            id = entry.optionalMetadata()
                    .flatMap(objectMeta -> objectMeta.findAnnotationByKey(AIVEN_IO_KAFKA_ACL_ID))
                    .map(Object::toString)
                    .orElse(null);
        }

        return new KafkaAclEntry(
                spec.getPermission().name().toLowerCase(Locale.ROOT),
                spec.getTopic(),
                spec.getUsername(),
                id
        );
    }

    public static KafkaAclEntry map(final V1KafkaTopicAclEntry entry) {
        return map(entry, NO_ENTRY_ID);
    }

    public static List<V1KafkaTopicAclEntry> map(final List<KafkaAclEntry> entries) {
        return entries
                .stream()
                .map(KafkaAclEntryAdapter::map)
                .toList();
    }

    public static V1KafkaTopicAclEntry map(final KafkaAclEntry entry) {
        if (entry == null) return null;
        ObjectMeta.ObjectMetaBuilder objectMetaBuilder = ObjectMeta.builder();
        if (entry.id() != null) {
            objectMetaBuilder = objectMetaBuilder
                    .withAnnotation(AIVEN_IO_KAFKA_ACL_ID, entry.id());
        }
        return V1KafkaTopicAclEntry
                .builder()
                .withMetadata(objectMetaBuilder.build())
                .withSpec(V1KafkaTopicAclEntrySpec
                        .builder()
                        .withUsername(entry.username())
                        .withTopic(entry.topic())
                        .withPermission(Permission.valueOf(entry.permission().toUpperCase(Locale.ROOT)))
                        .build()
                )
                .build();
    }

}
