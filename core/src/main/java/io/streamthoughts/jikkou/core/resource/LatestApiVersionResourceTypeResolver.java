/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import com.fasterxml.jackson.databind.JsonNode;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.models.ResourceType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolve the resource type with the latest api version.
 */
public class LatestApiVersionResourceTypeResolver implements ResourceTypeResolver {

    private final ResourceRegistry registry;

    /**
     * Creates a new {@link LatestApiVersionResourceTypeResolver} instance.
     *
     * @param registry  the {@link ResourceRegistry}.
     */
    public LatestApiVersionResourceTypeResolver(final ResourceRegistry registry) {
        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public @Nullable Class<? extends Resource> resolvesType(@NotNull JsonNode node) {
        ResourceType key = ResourceType.of(node);
        if (key == null || key.group() == null) {
            return null;
        }

        List<ResourceDescriptor> descriptors = registry.getDescriptorsByGroupAndKind(key.group(), key.kind());
        if (descriptors.isEmpty())
            return null;

        if (descriptors.size() == 1) {
            return descriptors.getFirst().resourceClass();
        }

        return resolveTypeForLatestApiVersion(descriptors);
    }

    private static Class<? extends Resource> resolveTypeForLatestApiVersion(List<ResourceDescriptor> descriptors) {
        Map<ApiVersion, ? extends Class<? extends Resource>> typesByVersion = descriptors
            .stream()
            .collect(Collectors.toMap(it -> ApiVersion.of(it.apiVersion()), ResourceDescriptor::resourceClass));

        ApiVersion latest = ApiVersion.getLatest(typesByVersion.keySet().toArray(new ApiVersion[0]));
        return typesByVersion.get(latest);
    }
}
