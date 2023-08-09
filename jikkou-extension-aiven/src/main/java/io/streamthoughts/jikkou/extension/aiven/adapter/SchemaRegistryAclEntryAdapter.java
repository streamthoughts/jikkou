/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.extension.aiven.adapter;

import static io.streamthoughts.jikkou.extension.aiven.MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntrySpec;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SchemaRegistryAclEntryAdapter {

    public static final @Nullable String NO_ENTRY_ID = null;

    public static SchemaRegistryAclEntry map(final @NotNull V1SchemaRegistryAclEntry entry, @Nullable String id) {
        V1SchemaRegistryAclEntrySpec spec = entry.getSpec();
        if (id == null) {
            id = entry.optionalMetadata()
                    .flatMap(objectMeta -> objectMeta.getAnnotation(AIVEN_IO_KAFKA_ACL_ID))
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

    public static SchemaRegistryAclEntry map(final @NotNull V1SchemaRegistryAclEntry entry) {
        return map(entry, NO_ENTRY_ID);
    }

    public static List<V1SchemaRegistryAclEntry> map(final @NotNull List<SchemaRegistryAclEntry> entries) {
        return  entries
                .stream()
                .map(SchemaRegistryAclEntryAdapter::map)
                .toList();
    }

    public static V1SchemaRegistryAclEntry map(final @NotNull SchemaRegistryAclEntry entry) {
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

    static class AivenPermissionMapper {

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
