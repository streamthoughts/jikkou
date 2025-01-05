/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.collections;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1beta1")
@Kind("KafkaConnectorList")
public class V1KafkaConnectorList extends SpecificResourceList<V1KafkaConnectorList, V1KafkaConnector> {


    /**
     * Creates a new {@link V1KafkaConnectorList} instance.
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
    public V1KafkaConnectorList(@Nullable String apiVersion,
                                @Nullable String kind,
                                @Nullable ObjectMeta metadata,
                                @NotNull List<V1KafkaConnector> items) {
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

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaConnectorList.Builder, V1KafkaConnectorList, V1KafkaConnector> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaConnectorList build() {
            return new V1KafkaConnectorList(apiVersion, kind, metadata, items);
        }
    }
}