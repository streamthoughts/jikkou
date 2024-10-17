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
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaTopicAclEntry;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.aiven.io/v1beta1")
@Kind("KafkaTopicAclEntryList")
public class V1KafkaTopicAclEntryList extends SpecificResourceList<V1KafkaTopicAclEntryList, V1KafkaTopicAclEntry> {


    /**
     * Creates a new {@link V1KafkaQuotaList} instance.
     *
     * @param kind       The resource Kind.
     * @param apiVersion The resource API Version.
     * @param metadata   The resource metadata.
     * @param items      The items.
     */
    @ConstructorProperties({
        "kind",
        "apiVersion",
        "metadata",
        "items"
    })
    public V1KafkaTopicAclEntryList(@Nullable String kind,
                                    @Nullable String apiVersion,
                                    @Nullable ObjectMeta metadata,
                                    @NotNull List<V1KafkaTopicAclEntry> items) {
        super(kind, apiVersion, metadata, items);
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

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaTopicAclEntryList.Builder, V1KafkaTopicAclEntryList, V1KafkaTopicAclEntry> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaTopicAclEntryList build() {
            return new V1KafkaTopicAclEntryList(apiVersion, kind, metadata, items);
        }
    }
}