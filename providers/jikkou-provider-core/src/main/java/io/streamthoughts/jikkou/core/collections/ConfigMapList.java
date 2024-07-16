/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.collections;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.ConfigMap;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.SpecificResourceList;
import java.beans.ConstructorProperties;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiVersion("core.jikkou.io/v1beta2")
@Kind("ConfigMapList")
@Reflectable
@JsonDeserialize(builder = ConfigMapList.Builder.class)
public class ConfigMapList extends SpecificResourceList<ConfigMapList, ConfigMap> {


    /**
     * Creates a new {@link ConfigMapList} instance.
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
    public ConfigMapList(@Nullable String kind,
                         @Nullable String apiVersion,
                         @Nullable ObjectMeta metadata,
                         @NotNull List<ConfigMap> items) {
        super(kind, apiVersion, metadata, items);
    }

    public ConfigMapList(final ObjectMeta metadata,
                         final List<ConfigMap> items) {
        super(metadata, items);
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder extends SpecificResourceList.Builder<ConfigMapList.Builder, ConfigMapList, ConfigMap> {
        /**
         * {@inheritDoc}
         */
        @Override
        public ConfigMapList build() {
            return new ConfigMapList(metadata, items);
        }
    }
}
