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
package io.streamthoughts.jikkou.api;

import io.streamthoughts.jikkou.api.error.JikkouApiException;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class ResourceContext {

    private final Map<ResourceType, ResourceDescriptor> descriptorsByType;
    private final Set<ResourceDescriptor> descriptors;

    /**
     * Creates a new {@link ResourceContext} instance.
     */
    public ResourceContext() {
        this.descriptorsByType = new HashMap<>();
        this.descriptors = new HashSet<>();
    }

    /**
     * Registers the given resource type to the context.
     *
     * @param resource  the resource class to register.
     */
    public void register(Class<? extends HasMetadata> resource) {
        ResourceDescriptor descriptor = new ResourceDescriptor(resource);
        ResourceDescriptor existing = descriptorsByType.putIfAbsent(descriptor.resourceType(), descriptor);
        if (existing != null) {
            throw new JikkouRuntimeException(
                "Cannot register resource for class '{" + resource.getName() + "}'. " +
                "Class already registered for " + descriptor.resourceType() + ": " + existing.resourceClass().getName());
        }
        this.descriptors.add(descriptor);
    }

    public ResourceDescriptor getResourceDescriptorByType(final @NotNull ResourceType type) {
        if (!descriptorsByType.containsKey(type)) {
            throw new JikkouApiException("No resource descriptor found for type: " + type);
        }
        return descriptorsByType.get(type);
    }

    /**
     * Gets all the resources register to the context.
     *
     * @return  all the registered resource type.
     */
    public List<ResourceDescriptor> getAllResourceDescriptors() {
        return new ArrayList<>(descriptors);
    }
}
