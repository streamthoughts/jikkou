/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.exception.ConflictingResourceDefinitionException;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * ResourceRegistry.
 */
public interface ResourceRegistry {

    /**
     * Registers the resource for the given type to the context.
     *
     * @param type     the resource type associated to the class.
     * @param resource the resource class to register.
     * @return the resource descriptor.
     * @throws NullPointerException                   if the resource class is {@code null}.
     * @throws ConflictingResourceDefinitionException if an extension is already register for that type.
     */
    ResourceDescriptor register(Class<? extends Resource> resource, ResourceType type);

    /**
     * Registers the resource for the given version to the context.
     *
     * @param version  the resource version associated to the class.
     * @param resource the resource class to register.
     * @return the resource descriptor.
     * @throws NullPointerException                   if the resource class is {@code null}.
     * @throws ConflictingResourceDefinitionException if an extension is already register for that type.
     */
    ResourceDescriptor register(Class<? extends Resource> resource, String version);

    /**
     * Registers the given resource type to the context.
     *
     * @param resource the resource class to register.
     * @return the resource descriptor.
     * @throws NullPointerException                   if the resource class is {@code null}.
     * @throws ConflictingResourceDefinitionException if an extension is already register for that type.
     */
    ResourceDescriptor register(Class<? extends Resource> resource);

    /**
     * Registers the given resource descriptor to the context.
     *
     * @param descriptor the resource descriptor.
     * @throws NullPointerException                   if the descriptor is {@code null}.
     * @throws ConflictingResourceDefinitionException if an extension is already register for that type.
     */
    ResourceDescriptor register(ResourceDescriptor descriptor);

    /**
     * Gets the resources descriptor for the specified type.
     *
     * @return all the registered resource type.
     */
    ResourceDescriptor getDescriptorByType(@NotNull ResourceType type);

    /**
     * Gets all the resources register to the context.
     *
     * @return The list of descriptors.
     */
    List<ResourceDescriptor> allDescriptors();

    /**
     * Gets all descriptors for the specified resource group.
     *
     * @param group The resource api group.
     * @return The list of descriptors.
     */
    List<ResourceDescriptor> getDescriptorsByGroup(final String group);

    /**
     * Gets all descriptors for the specified resource group and version.
     *
     * @param group The resource api group.
     * @return The list for descriptors.
     */
    List<ResourceDescriptor> getDescriptorsByGroupAndVersion(final String group,
                                                             final String version);

    /**
     * Finds a descriptor for the specified resource information.
     *
     * @param type The resource type.
     */
    Optional<ResourceDescriptor> findDescriptorByType(final ResourceType type);

    /**
     * Finds a descriptor for the specified resource information.
     *
     * @param kind          The kind of the resource.
     * @param group         The resource group.
     * @param version       The version of the resource.
     * @param caseSensitive Specify whether the kind is case-sensitive.
     * @return an optional {@link ResourceDescriptor}.
     */
    Optional<ResourceDescriptor> findDescriptorByType(final String kind,
                                                      final String group,
                                                      final String version,
                                                      final boolean caseSensitive);
}
