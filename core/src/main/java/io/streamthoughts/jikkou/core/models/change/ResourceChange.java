/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.models.change;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.models.HasSpec;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconciler.Change;
import io.streamthoughts.jikkou.core.reconciler.Operation;

/**
 * A resource change.
 */
@Reflectable
@JsonDeserialize(as = GenericResourceChange.class)
public interface ResourceChange extends HasSpec<ResourceChangeSpec>, Change {

    String RESOURCE_CHANGE_KIND_SUFFIX = "Change";

    /**
     * Gets a new objects with the given kind.
     *
     * @param apiVersion the kind of the resource.
     * @return a new {@link Resource}.
     */
    ResourceChange withApiVersion(final String apiVersion);

    /**
     * Gets a new objects with the given kind.
     *
     * @param kind the kind of the resource.
     * @return a new {@link Resource}.
     */
    ResourceChange withKind(final String kind);

    /** {@inheritDoc} **/
    @Override
    @JsonIgnore
    default Operation getOp() {
        return getSpec().getOp();
    }

    static String getChangeKindFromResource(Class<? extends Resource> resourceClass) {
        return Resource.getKind(resourceClass) + RESOURCE_CHANGE_KIND_SUFFIX;
    }

    static ResourceType fromResource(final Class<? extends Resource> resourceClass) {
        return ResourceType.of(
            ResourceChange.getChangeKindFromResource(resourceClass),
            Resource.getApiVersion(resourceClass)
        );
    }
}
