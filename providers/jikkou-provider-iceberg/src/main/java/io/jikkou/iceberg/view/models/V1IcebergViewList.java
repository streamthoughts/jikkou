/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.view.models;

import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.SpecificResourceList;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A list resource of {@link V1IcebergView} objects.
 */
@ApiVersion("iceberg.jikkou.io/v1beta1")
@Kind("IcebergViewList")
public class V1IcebergViewList extends SpecificResourceList<V1IcebergViewList, V1IcebergView> {

    /**
     * Creates a new {@link V1IcebergViewList} instance.
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
    public V1IcebergViewList(@Nullable String apiVersion,
                             @Nullable String kind,
                             @Nullable ObjectMeta metadata,
                             @NotNull List<V1IcebergView> items) {
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
     * Builder for {@link V1IcebergViewList}.
     */
    public static final class Builder
            extends SpecificResourceList.Builder<V1IcebergViewList.Builder, V1IcebergViewList, V1IcebergView> {

        /** {@inheritDoc} */
        @Override
        public V1IcebergViewList build() {
            return new V1IcebergViewList(apiVersion, kind, metadata, items);
        }
    }
}
