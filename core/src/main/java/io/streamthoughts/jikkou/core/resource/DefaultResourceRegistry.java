/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.exception.ConflictingResourceDefinitionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link ResourceRegistry}.
 */
public final class DefaultResourceRegistry implements ResourceRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultResourceRegistry.class);

    private final Map<ResourceType, ResourceDescriptor> descriptorsByType;
    private final Set<ResourceDescriptor> descriptors;
    private final ResourceDescriptorFactory factory;

    private final boolean doLog;

    /**
     * Creates a new {@link DefaultResourceRegistry} instance.
     */
    public DefaultResourceRegistry() {
        this(true);
    }

    /**
     * Creates a new {@link DefaultResourceRegistry} instance.
     */
    public DefaultResourceRegistry(boolean doLog) {
        this.doLog = doLog;
        this.descriptorsByType = new HashMap<>();
        this.descriptors = new HashSet<>();
        this.factory = new ResourceDescriptorFactory();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceDescriptor register(Class<? extends Resource> resource, ResourceType type) {
        Objects.requireNonNull(resource, "resource cannot be null");
        return register(factory.make(type, resource));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceDescriptor register(Class<? extends Resource> resource, String version) {
        Objects.requireNonNull(resource, "resource cannot be null");
        return register(resource, ResourceType.of(resource).version(version));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceDescriptor register(Class<? extends Resource> resource) {
        Objects.requireNonNull(resource, "resource cannot be null");
        return register(factory.make(ResourceType.of(resource), resource));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceDescriptor register(final ResourceDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor cannot be null");
        ResourceDescriptor existing = descriptorsByType.putIfAbsent(descriptor.resourceType(), descriptor);
        if (existing != null) {
            throw new ConflictingResourceDefinitionException(
                    "Cannot register resource for class '{" + descriptor.resourceClass().getName() + "}'. " +
                            "Class already registered for " + descriptor.resourceType() + ": " + existing.resourceClass().getName());
        }
        if (doLog) {
            LOG.info("Registered resource for group='{}', version='{}' and kind='{}'",
                    descriptor.group(),
                    descriptor.apiVersion(),
                    descriptor.kind()
            );
        }
        this.descriptors.add(descriptor);
        return descriptor;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceDescriptor getDescriptorByType(final @NotNull ResourceType type) {
        if (!descriptorsByType.containsKey(type)) {
            throw new NoSuchExtensionException("No resource registered found for type: " + type);
        }
        return descriptorsByType.get(type);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceDescriptor> allDescriptors() {
        return new ArrayList<>(descriptors);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<ResourceDescriptor> findDescriptorByType(ResourceType type) {
        return Optional.ofNullable(descriptorsByType.get(type));
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceDescriptor> getDescriptorsByGroup(final String group) {
        return descriptors.stream()
                .filter(descriptor -> descriptor.group().equalsIgnoreCase(group))
                .toList();

    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceDescriptor> getDescriptorsByGroupAndKind(final String group, final String kind) {
        return descriptors.stream()
            .filter(descriptor -> descriptor.group().equalsIgnoreCase(group))
            .filter(descriptors -> descriptors.kind().equalsIgnoreCase(kind))
            .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<ResourceDescriptor> getDescriptorsByGroupAndVersion(final String group,
                                                                    final String version) {
        return descriptors.stream()
                .filter(descriptor -> descriptor.group().equalsIgnoreCase(group))
                .filter(descriptor -> descriptor.apiVersion().equalsIgnoreCase(version))
                .toList();
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Optional<ResourceDescriptor> findDescriptorByType(final String kind,
                                                             final String group,
                                                             final String version,
                                                             final boolean caseSensitive) {
        if (caseSensitive) {
            return findDescriptorByType(new ResourceType(kind, group, version));
        }

        return descriptors.stream()
                .filter(descriptor -> {
                    if (!descriptor.group().equalsIgnoreCase(group)) {
                        return false;
                    }
                    if (!descriptor.apiVersion().equalsIgnoreCase(version)) {
                        return false;
                    }
                    return descriptor.kind().equalsIgnoreCase(kind);
                })
                .findFirst();

    }
}
