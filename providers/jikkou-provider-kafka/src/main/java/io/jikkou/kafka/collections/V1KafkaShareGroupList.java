/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.kafka.collections;

import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.SpecificResourceList;
import io.jikkou.kafka.models.V1KafkaShareGroup;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1")
@Kind("KafkaShareGroupList")
public class V1KafkaShareGroupList extends SpecificResourceList<V1KafkaShareGroupList, V1KafkaShareGroup> {

    /**
     * Creates a new {@link V1KafkaShareGroupList} instance.
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
    public V1KafkaShareGroupList(@Nullable String apiVersion,
                                 @Nullable String kind,
                                 @Nullable ObjectMeta metadata,
                                 @NotNull List<V1KafkaShareGroup> items) {
        super(apiVersion, kind, metadata, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaShareGroupList.Builder toBuilder() {
        return new V1KafkaShareGroupList.Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaShareGroupList.Builder, V1KafkaShareGroupList, V1KafkaShareGroup> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaShareGroupList build() {
            return new V1KafkaShareGroupList(apiVersion, kind, metadata, items);
        }
    }
}
