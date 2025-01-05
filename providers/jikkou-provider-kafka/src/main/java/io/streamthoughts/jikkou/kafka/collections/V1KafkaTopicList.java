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
import io.streamthoughts.jikkou.kafka.models.V1KafkaTopic;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1beta2")
@Kind("KafkaTopicList")
public class V1KafkaTopicList extends SpecificResourceList<V1KafkaTopicList, V1KafkaTopic> {


    /**
     * Creates a new {@link V1KafkaBrokerList} instance.
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
    public V1KafkaTopicList(@Nullable String apiVersion,
                            @Nullable String kind,
                            @Nullable ObjectMeta metadata,
                            @NotNull List<V1KafkaTopic> items) {
        super(apiVersion, kind, metadata, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaTopicList.Builder toBuilder() {
        return new V1KafkaTopicList.Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaTopicList.Builder, V1KafkaTopicList, V1KafkaTopic> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaTopicList build() {
            return new V1KafkaTopicList(apiVersion, kind, metadata, items);
        }
    }
}
