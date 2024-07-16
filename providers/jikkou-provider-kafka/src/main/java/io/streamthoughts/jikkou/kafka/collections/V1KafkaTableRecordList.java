/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.collections;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import io.streamthoughts.jikkou.kafka.models.V1KafkaTableRecord;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1beta2")
@Kind("KafkaTableRecordList")
public class V1KafkaTableRecordList extends SpecificResourceList<V1KafkaTableRecordList, V1KafkaTableRecord> {


    /**
     * Creates a new {@link V1KafkaBrokerList} instance.
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
    public V1KafkaTableRecordList(@Nullable String kind,
                                  @Nullable String apiVersion,
                                  @Nullable ObjectMeta metadata,
                                  @NotNull List<V1KafkaTableRecord> items) {
        super(kind, apiVersion, metadata, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaTableRecordList.Builder toBuilder() {
        return new V1KafkaTableRecordList.Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaTableRecordList.Builder, V1KafkaTableRecordList, V1KafkaTableRecord> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaTableRecordList build() {
            return new V1KafkaTableRecordList(apiVersion, kind, metadata, items);
        }
    }
}
