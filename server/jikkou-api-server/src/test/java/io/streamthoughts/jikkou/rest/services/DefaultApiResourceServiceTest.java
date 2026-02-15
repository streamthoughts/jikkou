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
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import io.streamthoughts.jikkou.core.reconciler.SimpleResourceChangeFilter;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.rest.exception.ApiResourceNotFoundException;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class DefaultApiResourceServiceTest {

    private static final ResourceType RESOURCE_TYPE = new ResourceType("Resource", "group", "1");
    private static final ResourceDescriptor DESCRIPTOR = new ResourceDescriptor(
            RESOURCE_TYPE,
            "",
            HasMetadata.class,
            "singularName",
            "pluralName",
            Collections.emptySet(),
            Collections.emptySet(),
            false
    );
    private static final ApiResourceIdentifier IDENTIFIER = new ApiResourceIdentifier("group", "1", "pluralName");

    private JikkouApi mockApi;
    private DefaultApiResourceService service;

    @BeforeEach
    void setUp() {
        mockApi = Mockito.mock(JikkouApi.class);
        DefaultResourceRegistry registry = new DefaultResourceRegistry();
        registry.register(DESCRIPTOR);
        service = new DefaultApiResourceService(mockApi, registry);
    }

    @Test
    void shouldReturnNonNullForGetDescriptorByIdentifier() {
        // When
        ResourceDescriptor result = service.getResourceDescriptorByIdentifier(IDENTIFIER);

        // Then
        Assertions.assertEquals(DESCRIPTOR, result);
    }

    @Test
    void shouldThrowExceptionForGetDescriptorByIdentifierWhenResourceNotFound() {
        // Given
        ApiResourceIdentifier unknown = new ApiResourceIdentifier("???", "???", "???");

        // When / Then
        ApiResourceNotFoundException ex = Assertions.assertThrows(ApiResourceNotFoundException.class, () -> {
            service.getResourceDescriptorByIdentifier(unknown);
        });
        Assertions.assertEquals(unknown, ex.identifier());
    }

    @Test
    void shouldDelegateToApiListResourcesWhenSearchCalled() {
        // Given
        ListContext context = ListContext.Default.EMPTY;
        ResourceList<HasMetadata> expected = ResourceList.empty();
        Mockito.when(mockApi.listResources(RESOURCE_TYPE, context)).thenReturn(expected);

        // When
        ResourceList<HasMetadata> result = service.search(IDENTIFIER, context);

        // Then
        Assertions.assertSame(expected, result);
        Mockito.verify(mockApi).listResources(RESOURCE_TYPE, context);
    }

    @Test
    void shouldDelegateToApiGetResourceWhenGetCalled() {
        // Given
        Configuration config = Configuration.empty();
        HasMetadata expected = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        Mockito.when(mockApi.getResource(RESOURCE_TYPE, "my-resource", config)).thenReturn(expected);

        // When
        HasMetadata result = service.get(IDENTIFIER, "my-resource", config);

        // Then
        Assertions.assertSame(expected, result);
        Mockito.verify(mockApi).getResource(RESOURCE_TYPE, "my-resource", config);
    }

    @Test
    void shouldDelegateToApiReconcileWhenReconcileCalled() {
        // Given
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        HasMetadata resource = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        ApiChangeResultList expected = Mockito.mock(ApiChangeResultList.class);
        Mockito.when(mockApi.reconcile(Mockito.any(HasItems.class), Mockito.eq(ReconciliationMode.CREATE), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        ApiChangeResultList result = service.reconcile(IDENTIFIER, ReconciliationMode.CREATE, List.of(resource), context);

        // Then
        Assertions.assertSame(expected, result);
        Mockito.verify(mockApi).reconcile(Mockito.any(HasItems.class), Mockito.eq(ReconciliationMode.CREATE), Mockito.eq(context));
    }

    @Test
    void shouldDelegateToApiPatchWhenPatchCalled() {
        // Given
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        HasMetadata resource = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        ApiChangeResultList expected = Mockito.mock(ApiChangeResultList.class);
        Mockito.when(mockApi.patch(Mockito.any(HasItems.class), Mockito.eq(ReconciliationMode.CREATE), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        ApiChangeResultList result = service.patch(ReconciliationMode.CREATE, List.of(resource), context);

        // Then
        Assertions.assertSame(expected, result);
        Mockito.verify(mockApi).patch(Mockito.any(HasItems.class), Mockito.eq(ReconciliationMode.CREATE), Mockito.eq(context));
    }

    @Test
    void shouldDelegateToApiGetDiffWhenDiffCalled() {
        // Given
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        HasMetadata resource = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        SimpleResourceChangeFilter filter = new SimpleResourceChangeFilter();
        ApiResourceChangeList expected = Mockito.mock(ApiResourceChangeList.class);
        Mockito.when(mockApi.getDiff(Mockito.any(HasItems.class), Mockito.eq(filter), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        ApiResourceChangeList result = service.diff(IDENTIFIER, List.of(resource), filter, context);

        // Then
        Assertions.assertSame(expected, result);
        Mockito.verify(mockApi).getDiff(Mockito.any(HasItems.class), Mockito.eq(filter), Mockito.eq(context));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDelegateToApiValidateWhenValidateCalled() {
        // Given
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        HasMetadata resource = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        ResourceList<HasMetadata> validatedResources = ResourceList.of(List.of(resource));
        ApiValidationResult<HasMetadata> validationResult = new ApiValidationResult<>(validatedResources);
        Mockito.when(mockApi.validate(Mockito.any(HasItems.class), Mockito.eq(context)))
                .thenReturn(validationResult);

        // When
        ResourceList<HasMetadata> result = service.validate(IDENTIFIER, List.of(resource), context);

        // Then
        Assertions.assertNotNull(result);
        Mockito.verify(mockApi).validate(Mockito.any(HasItems.class), Mockito.eq(context));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnResourceListWithCorrectKindWhenValidateCalled() {
        // Given
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        HasMetadata resource = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        ResourceList<HasMetadata> validatedResources = ResourceList.of(List.of(resource));
        ApiValidationResult<HasMetadata> validationResult = new ApiValidationResult<>(validatedResources);
        Mockito.when(mockApi.validate(Mockito.any(HasItems.class), Mockito.eq(context)))
                .thenReturn(validationResult);

        // When
        ResourceList<HasMetadata> result = service.validate(IDENTIFIER, List.of(resource), context);

        // Then
        Assertions.assertEquals("ResourceList", result.getKind());
        Assertions.assertEquals("group/1", result.getApiVersion());
    }

    @Test
    void shouldThrowApiResourceNotFoundExceptionWhenSearchCalledWithUnknownIdentifier() {
        // Given
        ApiResourceIdentifier unknown = new ApiResourceIdentifier("unknown", "v1", "resources");
        ListContext context = ListContext.Default.EMPTY;

        // When / Then
        Assertions.assertThrows(ApiResourceNotFoundException.class, () -> {
            service.search(unknown, context);
        });
    }

    @Test
    void shouldFilterResourcesByTypeWhenReconcileCalled() {
        // Given
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        HasMetadata matchingResource = new GenericResource("group/1", "Resource", new ObjectMeta(), null);
        HasMetadata nonMatchingResource = new GenericResource("other/v1", "OtherKind", new ObjectMeta(), null);
        ApiChangeResultList expected = Mockito.mock(ApiChangeResultList.class);

        ArgumentCaptor<HasItems> captor = ArgumentCaptor.forClass(HasItems.class);
        Mockito.when(mockApi.reconcile(captor.capture(), Mockito.eq(ReconciliationMode.CREATE), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        service.reconcile(IDENTIFIER, ReconciliationMode.CREATE, List.of(matchingResource, nonMatchingResource), context);

        // Then
        HasItems captured = captor.getValue();
        Assertions.assertEquals(1, captured.getItems().size());
    }
}
