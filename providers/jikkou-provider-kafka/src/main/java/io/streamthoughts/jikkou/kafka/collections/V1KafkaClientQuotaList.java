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
import io.streamthoughts.jikkou.kafka.models.V1KafkaClientQuota;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1")
@Kind("KafkaClientQuotaList")
public class V1KafkaClientQuotaList extends SpecificResourceList<V1KafkaClientQuotaList, V1KafkaClientQuota> {


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
    public V1KafkaClientQuotaList(@Nullable String apiVersion,
                                  @Nullable String kind,
                                  @Nullable ObjectMeta metadata,
                                  @NotNull List<V1KafkaClientQuota> items) {
        super(apiVersion, kind, metadata, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaClientQuotaList.Builder toBuilder() {
        return new V1KafkaClientQuotaList.Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaClientQuotaList.Builder, V1KafkaClientQuotaList, V1KafkaClientQuota> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaClientQuotaList build() {
            return new V1KafkaClientQuotaList(apiVersion, kind, metadata, items);
        }
    }
}
