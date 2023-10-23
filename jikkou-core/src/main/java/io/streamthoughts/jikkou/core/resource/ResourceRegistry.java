/*
 * Copyright 2023 The original authors
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
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface ResourceRegistry {

    /**
     * Registers the resource for the given type to the context.
     *
     * @param type    the resource type associated to the class.
     * @param resource the resource class to register.
     *
     * @return the resource descriptor.
     */
    ResourceDescriptor register(Class<? extends HasMetadata> resource, ResourceType type);
    /**
     * Registers the resource for the given version to the context.
     *
     * @param version the resource version associated to the class.
     * @param resource the resource class to register.
     *
     * @return the resource descriptor.
     */
    ResourceDescriptor register(Class<? extends HasMetadata> resource, String version);
    /**
     * Registers the given resource type to the context.
     *
     * @param resource the resource class to register.
     * @return the resource descriptor.
     */
    ResourceDescriptor register(Class<? extends HasMetadata> resource);
    /**
     * Registers the given resource descriptor to the context.
     *
     * @param descriptor the resource descriptor.
     */
    ResourceDescriptor register(ResourceDescriptor descriptor);

    /**
     * Gets the resources descriptor for the specified type.
     *
     * @return all the registered resource type.
     */
    ResourceDescriptor getResourceDescriptorByType(@NotNull ResourceType type);

    /**
     * Gets all the resources register to the context.
     *
     * @return all the registered resource type.
     */
    List<ResourceDescriptor> getAllResourceDescriptors();
}
