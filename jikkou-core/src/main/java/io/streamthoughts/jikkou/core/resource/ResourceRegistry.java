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

import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface ResourceRegistry {

    /**
     * Registers the resource for the given type to the context.
     *
     * @param type     the resource type associated to the class.
     * @param resource the resource class to register.
     * @return the resource descriptor.
     */
    ResourceDescriptor register(Class<? extends Resource> resource, ResourceType type);

    /**
     * Registers the resource for the given version to the context.
     *
     * @param version  the resource version associated to the class.
     * @param resource the resource class to register.
     * @return the resource descriptor.
     */
    ResourceDescriptor register(Class<? extends Resource> resource, String version);

    /**
     * Registers the given resource type to the context.
     *
     * @param resource the resource class to register.
     * @return the resource descriptor.
     */
    ResourceDescriptor register(Class<? extends Resource> resource);

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

    /**
     * Finds a descriptor for the specified resource information.
     *
     * @param type the resource type.
     */
    Optional<ResourceDescriptor> findDescriptorByType(final ResourceType type);

    /**
     * Gets all descriptors for the specified resource group.
     *
     * @param group the resource api group.
     * @return the list for descriptors.
     */
    List<ResourceDescriptor> findDescriptorsByGroup(final String group);

    /**
     * Gets all descriptors for the specified resource group and version.
     *
     * @param group the resource api group.
     * @return the list for descriptors.
     */
    List<ResourceDescriptor> findDescriptorsByGroupVersion(final String group,
                                                           final String version);

    /**
     * Gets a descriptor for the specified resource information.
     *
     * @param kind          the kind of the resource.
     * @param group         the resource group.
     * @param version       the version of the resource.
     * @param caseSensitive specify if the kind is case-sensitive.
     * @return  an optional {@link ResourceDescriptor}.
     */
    Optional<ResourceDescriptor> findDescriptorByType(final String kind,
                                                      final String group,
                                                      final String version,
                                                      final boolean caseSensitive);
}
