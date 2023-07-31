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
package io.streamthoughts.jikkou.extension.aiven.change;

import static io.streamthoughts.jikkou.api.control.ChangeType.ADD;
import static io.streamthoughts.jikkou.api.control.ChangeType.DELETE;
import static io.streamthoughts.jikkou.api.control.ChangeType.IGNORE;
import static io.streamthoughts.jikkou.api.control.ChangeType.UPDATE;

import io.streamthoughts.jikkou.JikkouMetadataAnnotations;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.control.ValueChangeComputer;
import io.streamthoughts.jikkou.extension.aiven.adapter.SchemaRegistryAclEntryAdapter;
import io.streamthoughts.jikkou.extension.aiven.api.data.Permission;
import io.streamthoughts.jikkou.extension.aiven.api.data.SchemaRegistryAclEntry;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SchemaRegistryAclEntryChangeComputer extends ValueChangeComputer<V1SchemaRegistryAclEntry, SchemaRegistryAclEntry> {


    /**
     * Creates a new {@link SchemaRegistryAclEntryChangeComputer} instance.
     *
     * @param deleteOrphans flag to indicate if orphans entries must be deleted.
     */
    public SchemaRegistryAclEntryChangeComputer(boolean deleteOrphans) {
        super(new KeyMapper(), new ValueMapper(), deleteOrphans);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    protected ChangeType getChangeType(V1SchemaRegistryAclEntry before, V1SchemaRegistryAclEntry after) {
        if (before == null && after == null)
            return IGNORE;

        if (before == null)
            return JikkouMetadataAnnotations.isAnnotatedWithDelete(after) ? IGNORE : ADD;

        if (after == null)
            return DELETE;

        return JikkouMetadataAnnotations.isAnnotatedWithDelete(after) ? DELETE : UPDATE;
    }


    record Key(String username, String resource, Permission permission) {
    }

    static class KeyMapper implements ChangeKeyMapper<V1SchemaRegistryAclEntry> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public @NotNull Object apply(@NotNull V1SchemaRegistryAclEntry o) {
            return new Key(
                    o.getSpec().getUsername(),
                    o.getSpec().getResource(),
                    o.getSpec().getPermission()
            );
        }
    }


    static class ValueMapper implements ChangeValueMapper<V1SchemaRegistryAclEntry, SchemaRegistryAclEntry> {
        /**
         * {@inheritDoc}
         **/
        @Override
        public @NotNull SchemaRegistryAclEntry apply(@Nullable V1SchemaRegistryAclEntry before,
                                                     @Nullable V1SchemaRegistryAclEntry after) {
            if (before == null && after != null)
                return SchemaRegistryAclEntryAdapter.map(after);
            if (before != null) {
                return SchemaRegistryAclEntryAdapter.map(before);
            }
            throw new IllegalArgumentException("both arguments are null");
        }
    }
}
