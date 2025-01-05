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
import io.streamthoughts.jikkou.extension.aiven.models.V1KafkaQuota;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.aiven.io/v1beta1")
@Kind("KafkaQuotaList")
public class V1KafkaQuotaList extends SpecificResourceList<V1KafkaQuotaList, V1KafkaQuota> {


    /**
     * Creates a new {@link V1KafkaQuotaList} instance.
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
    public V1KafkaQuotaList(@Nullable String apiVersion,
                            @Nullable String kind,
                            @Nullable ObjectMeta metadata,
                            @NotNull List<V1KafkaQuota> items) {
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

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaQuotaList.Builder, V1KafkaQuotaList, V1KafkaQuota> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaQuotaList build() {
            return new V1KafkaQuotaList(apiVersion, kind, metadata, items);
        }
    }
}