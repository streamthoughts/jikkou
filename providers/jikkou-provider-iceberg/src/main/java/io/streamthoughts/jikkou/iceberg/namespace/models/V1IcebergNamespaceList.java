/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.iceberg.namespace.models;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A list resource of {@link V1IcebergNamespace} objects.
 */
@ApiVersion("iceberg.jikkou.io/v1beta1")
@Kind("IcebergNamespaceList")
public class V1IcebergNamespaceList extends SpecificResourceList<V1IcebergNamespaceList, V1IcebergNamespace> {

    /**
     * Creates a new {@link V1IcebergNamespaceList} instance.
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
    public V1IcebergNamespaceList(@Nullable String apiVersion,
                                  @Nullable String kind,
                                  @Nullable ObjectMeta metadata,
                                  @NotNull List<V1IcebergNamespace> items) {
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
     * Builder for {@link V1IcebergNamespaceList}.
     */
    public static final class Builder
            extends SpecificResourceList.Builder<V1IcebergNamespaceList.Builder, V1IcebergNamespaceList, V1IcebergNamespace> {

        /** {@inheritDoc} */
        @Override
        public V1IcebergNamespaceList build() {
            return new V1IcebergNamespaceList(apiVersion, kind, metadata, items);
        }
    }
}
