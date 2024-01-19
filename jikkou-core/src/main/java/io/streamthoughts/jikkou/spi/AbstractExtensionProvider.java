/*
 * Copyright 2024 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
