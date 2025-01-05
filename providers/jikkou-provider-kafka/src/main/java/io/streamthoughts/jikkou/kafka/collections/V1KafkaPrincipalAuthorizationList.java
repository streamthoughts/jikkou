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
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("kafka.jikkou.io/v1beta2")
@Kind("KafkaPrincipalAuthorizationList")
public class V1KafkaPrincipalAuthorizationList extends SpecificResourceList<V1KafkaPrincipalAuthorizationList, V1KafkaPrincipalAuthorization> {

    /**
     * Creates a new {@link V1KafkaPrincipalAuthorizationList} instance.
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
    public V1KafkaPrincipalAuthorizationList(@Nullable String apiVersion,
                                             @Nullable String kind,
                                             @Nullable ObjectMeta metadata,
                                             @NotNull List<V1KafkaPrincipalAuthorization> items) {
        super(apiVersion, kind, metadata, items);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V1KafkaPrincipalAuthorizationList.Builder toBuilder() {
        return new V1KafkaPrincipalAuthorizationList.Builder()
            .withApiVersion(apiVersion)
            .withKind(kind)
            .withMetadata(metadata)
            .withItems(items);
    }

    public static final class Builder extends SpecificResourceList.Builder<V1KafkaPrincipalAuthorizationList.Builder, V1KafkaPrincipalAuthorizationList, V1KafkaPrincipalAuthorization> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1KafkaPrincipalAuthorizationList build() {
            return new V1KafkaPrincipalAuthorizationList(apiVersion, kind, metadata, items);
        }
    }
}
