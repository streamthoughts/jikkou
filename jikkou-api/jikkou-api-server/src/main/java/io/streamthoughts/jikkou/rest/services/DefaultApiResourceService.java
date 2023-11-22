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
package io.streamthoughts.jikkou.rest.services;

import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiValidationResult;
import io.streamthoughts.jikkou.core.models.DefaultResourceListObject;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.core.resource.ResourceRegistry;
import io.streamthoughts.jikkou.rest.exception.ApiResourceNotFoundException;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

@Singleton
public final class DefaultApiResourceService implements ApiResourceService {

    public static final String LIST_KIND_SUFFIX = "List";

    private final JikkouApi api;
    private final ResourceRegistry resourceRegistry;

    /**
     * Creates a new {@link DefaultApiResourceService} instance.
     *
     * @param api              the Jikkou API.
     * @param resourceRegistry the JikkouContext context.
     */
    @Inject
    public DefaultApiResourceService(JikkouApi api, ResourceRegistry resourceRegistry) {
        this.api = Objects.requireNonNull(api, "api must not be null");
        this.resourceRegistry = Objects.requireNonNull(resourceRegistry, "resourceRegistry must not be null");
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiChangeResultList reconcile(ApiResourceIdentifier identifier,
                                         ReconciliationMode mode,
                                         List<HasMetadata> resources,
                                         ReconciliationContext context) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        return api.reconcile(filterResources(resources, descriptor), mode, context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceChangeList diff(ApiResourceIdentifier identifier, List<HasMetadata> resources, ReconciliationContext context) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        return api.getDiff(filterResources(resources, descriptor), context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<HasMetadata> validate(ApiResourceIdentifier identifier,
                                                    List<HasMetadata> resources,
                                                    ReconciliationContext context) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        ApiValidationResult result = api.validate(filterResources(resources, descriptor), context);
        return toResourceListObject(descriptor, result.get().getItems());
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceListObject<HasMetadata> search(ApiResourceIdentifier identifier,
                                                  ReconciliationContext context) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        return api.listResources(
                descriptor.resourceType(),
                context.selector(),
                context.configuration()
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public HasMetadata get(ApiResourceIdentifier identifier,
                           String name,
                           Configuration configuration) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        return api.getResource(
                descriptor.resourceType(),
                name,
                configuration
        );
    }

    private ResourceListObject<HasMetadata> toResourceListObject(ResourceDescriptor descriptor,
                                                                 List<HasMetadata> items) {
        return DefaultResourceListObject.builder()
                .withApiVersion(descriptor.group() + "/" + descriptor.apiVersion())
                .withKind(descriptor.kind() + LIST_KIND_SUFFIX)
                .withMetadata(new ObjectMeta())
                .withItems(items)
                .build();
    }

    /**
     * Finds the descriptor for the specified resource identifier.
     *
     * @param identifier the resource identifier. Cannot be null.
     * @return an optional {@link ResourceDescriptor}.
     * @throws NullPointerException if the specified identifier is {@code null}.
     */
    @NotNull
    @VisibleForTesting
    ResourceDescriptor getResourceDescriptorByIdentifier(@NotNull ApiResourceIdentifier identifier) {
        Objects.requireNonNull(identifier, "identifier must not be null");
        return resourceRegistry
                .getDescriptorsByGroupAndVersion(identifier.group(), identifier.version())
                .stream()
                .filter(Predicate.not(ResourceDescriptor::isResourceListObject))
                .filter(ResourceDescriptor::isEnabled)
                .filter(descriptor -> {
                    String name = descriptor.pluralName()
                            .orElse(descriptor.resourceType().kind())
                            .toLowerCase(Locale.ROOT);
                    return identifier.plural().equalsIgnoreCase(name);
                })
                .findFirst()
                .orElseThrow(() -> new ApiResourceNotFoundException(identifier));
    }

    @NotNull
    private ResourceListObject<HasMetadata> filterResources(List<HasMetadata> resources,
                                                            ResourceDescriptor descriptor) {

        DefaultResourceListObject<HasMetadata> items = new DefaultResourceListObject<>(resources);
        return new DefaultResourceListObject<>(items.getAllByType(descriptor.resourceType()));
    }
}
