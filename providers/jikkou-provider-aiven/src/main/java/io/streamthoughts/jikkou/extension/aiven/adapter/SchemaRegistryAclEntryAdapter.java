/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import static io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class SchemaRegistryAclEntryAdapter {

    public static final @Nullable String NO_ENTRY_ID = null;

    public static SchemaRegistryAclEntry map(final V1SchemaRegistryAclEntry entry,
                                             String id) {
        if (entry == null) return null;

        V1SchemaRegistryAclEntrySpec spec = entry.getSpec();
        if (id == null) {
            id = entry.optionalMetadata()
                    .flatMap(objectMeta -> objectMeta.findAnnotationByKey(AIVEN_IO_KAFKA_ACL_ID))
                    .map(Object::toString)
                    .orElse(null);
        }

        return new SchemaRegistryAclEntry(
                AivenPermissionMapper.map(spec.getPermission()),
                spec.getResource(),
                spec.getUsername(),
                id
        );
    }

    public static SchemaRegistryAclEntry map(final V1SchemaRegistryAclEntry entry) {
        return map(entry, NO_ENTRY_ID);
    }

    public static List<V1SchemaRegistryAclEntry> map(final List<SchemaRegistryAclEntry> entries) {
        return entries
                .stream()
                .map(SchemaRegistryAclEntryAdapter::map)
                .toList();
    }

    public static V1SchemaRegistryAclEntry map(final SchemaRegistryAclEntry entry) {
        if (entry == null) return null;
        ObjectMeta.ObjectMetaBuilder objectMetaBuilder = ObjectMeta.builder();
        if (entry.id() != null) {
            objectMetaBuilder = objectMetaBuilder
                    .withAnnotation(AIVEN_IO_KAFKA_ACL_ID, entry.id());
        }
        return V1SchemaRegistryAclEntry.builder()
                .withMetadata(objectMetaBuilder.build())
                .withSpec(V1SchemaRegistryAclEntrySpec
                        .builder()
                        .withUsername(entry.username())
                        .withResource(entry.resource())
                        .withPermission(AivenPermissionMapper.map(entry.permission()))
                        .build()
                )
                .build();
    }

    public static class AivenPermissionMapper {

        public static final String SCHEMA_REGISTRY_WRITE = "schema_registry_write";
        public static final String SCHEMA_REGISTRY_READ = "schema_registry_read";

        public static Permission map(String permission) {
            if (permission.equalsIgnoreCase(SCHEMA_REGISTRY_WRITE)) {
                return Permission.WRITE;
            }
            if (permission.equalsIgnoreCase(SCHEMA_REGISTRY_READ)) {
                return Permission.READ;
            }
            return Permission.valueOf(permission.toUpperCase(Locale.ROOT));
        }

        public static String map(Permission permission) {
            if (permission == Permission.WRITE) {
                return SCHEMA_REGISTRY_WRITE;
            }
            if (permission == Permission.READ) {
                return SCHEMA_REGISTRY_READ;
            }
            return permission.name().toLowerCase(Locale.ROOT);
        }
    }
}
