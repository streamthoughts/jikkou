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
import io.streamthoughts.jikkou.kafka.models.V1KafkaBroker;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1beta2")
@Kind("KafkaBrokerList")
public class V1KafkaBrokerList extends SpecificResourceList<V1KafkaBrokerList, V1KafkaBroker> {


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
    public V1KafkaBrokerList(@Nullable String kind,
                             @Nullable String apiVersion,
                             @Nullable ObjectMeta metadata,
                             @NotNull List<V1KafkaBroker> items) {
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

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaBrokerList.Builder, V1KafkaBrokerList, V1KafkaBroker> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaBrokerList build() {
            return new V1KafkaBrokerList(apiVersion, kind, metadata, items);
        }
    }
}
