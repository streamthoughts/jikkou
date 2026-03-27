/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.table.models;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A list resource of {@link V1IcebergTable} objects.
 */
@ApiVersion("iceberg.jikkou.io/v1beta1")
@Kind("IcebergTableList")
public class V1IcebergTableList extends SpecificResourceList<V1IcebergTableList, V1IcebergTable> {

    /**
     * Creates a new {@link V1IcebergTableList} instance.
     *
     * @param apiVersion the resource API Version.
     * @param kind       the resource Kind.
     * @param metadata   the resource metadata.
     * @param items      the items.
     */
    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "items"
    })
    public V1IcebergTableList(@Nullable String apiVersion,
                              @Nullable String kind,
                              @Nullable ObjectMeta metadata,
                              @NotNull List<V1IcebergTable> items) {
        super(apiVersion, kind, metadata, items);
    }

    /** {@inheritDoc} */
    @Override
    public Builder toBuilder() {
        return new Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    /**
     * Builder for {@link V1IcebergTableList}.
     */
    public static final class Builder
            extends SpecificResourceList.Builder<V1IcebergTableList.Builder, V1IcebergTableList, V1IcebergTable> {

        /** {@inheritDoc} */
        @Override
        public V1IcebergTableList build() {
            return new V1IcebergTableList(apiVersion, kind, metadata, items);
        }
    }
}
