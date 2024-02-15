/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.spi;

import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractExtensionProvider implements ExtensionProvider {

    /**
     * Utility method to register the resource with its change kind.
     *
     * @param registry  the ResourceRegistry.
     * @param resource  the resource.
     */
    protected void registerResource(@NotNull ResourceRegistry registry,
                                    @NotNull Class<? extends Resource> resource) {

        registry.register(resource);
        if (!ResourceListObject.class.isAssignableFrom(resource)) {
            ResourceType type = ResourceType.of(
                ResourceChange.getChangeKindFromResource(resource),
                Resource.getApiVersion(resource)
            );
            registry.register(GenericResourceChange.class, type);
        }
    }
}
