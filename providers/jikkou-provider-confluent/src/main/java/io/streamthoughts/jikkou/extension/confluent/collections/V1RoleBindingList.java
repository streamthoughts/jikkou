/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.collections;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import io.streamthoughts.jikkou.extension.confluent.models.V1RoleBinding;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("iam.confluent.cloud/v1")
@Kind("RoleBindingList")
public class V1RoleBindingList extends SpecificResourceList<V1RoleBindingList, V1RoleBinding> {

    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "items"
    })
    public V1RoleBindingList(@Nullable String apiVersion,
                             @Nullable String kind,
                             @Nullable ObjectMeta metadata,
                             @NotNull List<V1RoleBinding> items) {
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

    public static final class Builder extends SpecificResourceList.Builder<V1RoleBindingList.Builder, V1RoleBindingList, V1RoleBinding> {
        /**
         * {@inheritDoc}
         */
        @Override
        public V1RoleBindingList build() {
            return new V1RoleBindingList(apiVersion, kind, metadata, items);
        }
    }
}
