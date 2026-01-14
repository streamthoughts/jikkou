/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.services;

import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ListContext;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.ApiValidationResult;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.generics.GenericResourceList;
import io.streamthoughts.jikkou.core.reconciler.ResourceChangeFilter;
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
    public ApiChangeResultList patch(ReconciliationMode mode,
                                     List<HasMetadata> resources,
                                     ReconciliationContext context) {
        return api.patch(ResourceList.of(resources), mode, context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ApiResourceChangeList diff(ApiResourceIdentifier identifier,
                                      List<HasMetadata> resources,
                                      ResourceChangeFilter filter,
                                      ReconciliationContext context) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        return api.getDiff(filterResources(resources, descriptor), filter, context);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public ResourceList<HasMetadata> validate(ApiResourceIdentifier identifier,
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
    public ResourceList<HasMetadata> search(ApiResourceIdentifier identifier,
                                            ListContext context) {
        ResourceDescriptor descriptor = getResourceDescriptorByIdentifier(identifier);
        return api.listResources(
            descriptor.resourceType(),
            context
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

    private ResourceList<HasMetadata> toResourceListObject(ResourceDescriptor descriptor,
                                                           List<HasMetadata> items) {
        return new GenericResourceList.Builder<>()
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
    private ResourceList<HasMetadata> filterResources(List<HasMetadata> resources,
                                                      ResourceDescriptor descriptor) {

        @SuppressWarnings("unchecked")
        List<HasMetadata> allByType = (List<HasMetadata>) ResourceList
            .of(resources)
            .getAllByType(descriptor.resourceType());
        return ResourceList.of(allByType);
    }
}
