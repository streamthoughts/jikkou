/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.collections;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.connect.models.V1KafkaConnector;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1beta1")
@Kind("KafkaConnectorList")
public class V1KafkaConnectorList extends DefaultResourceListObject<V1KafkaConnector> {


    /**
     * Creates a new {@link V1KafkaConnectorList} instance.
     *
     * @param kind       The resource Kind.
     * @param apiVersion The resource API Version.
     * @param metadata   The resource metadata.
     * @param items      The items.
     */
    @ConstructorProperties({
            "apiVersion",
            "kind",
            "metadata",
            "items"
    })
    public V1KafkaConnectorList(@Nullable String kind,
                            @Nullable String apiVersion,
                            @Nullable ObjectMeta metadata,
                            @NotNull List<? extends V1KafkaConnector> items) {
        super(kind, apiVersion, metadata, items);
    }

    /**
     * Creates a new {@link V1KafkaConnectorList} instance.
     *
     * @param items The items.
     */
    public V1KafkaConnectorList(List<? extends V1KafkaConnector> items) {
        super(items);
    }
}