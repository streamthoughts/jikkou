/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.collections;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import io.streamthoughts.jikkou.extension.aiven.models.V1SchemaRegistryAclEntry;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.aiven.io/v1")
@Kind("SchemaRegistryAclEntryList")
public class V1SchemaRegistryAclEntryList extends SpecificResourceList<V1SchemaRegistryAclEntryList, V1SchemaRegistryAclEntry> {


    /**
     * Creates a new {@link V1KafkaQuotaList} instance.
     *
     * @param apiVersion The resource API Version.
     * @param kind       The resource Kind.
     * @param metadata   The resource metadata.
     * @param items      The items.
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "items"
    })
    public V1SchemaRegistryAclEntryList(@Nullable String apiVersion,
                                        @Nullable String kind,
                                        @Nullable ObjectMeta metadata,
                                        @NotNull List<V1SchemaRegistryAclEntry> items) {
        super(apiVersion, kind, metadata, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Builder toBuilder() {
        return new Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    public static final class Builder extends SpecificResourceList.Builder<V1SchemaRegistryAclEntryList.Builder, V1SchemaRegistryAclEntryList, V1SchemaRegistryAclEntry> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1SchemaRegistryAclEntryList build() {
            return new V1SchemaRegistryAclEntryList(apiVersion, kind, metadata, items);
        }
    }
}
