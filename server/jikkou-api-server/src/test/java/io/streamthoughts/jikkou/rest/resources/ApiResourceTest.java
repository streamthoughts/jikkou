/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.resources;

import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.streamthoughts.jikkou.core.ListContext;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import io.streamthoughts.jikkou.rest.adapters.ReconciliationContextAdapter;
import io.streamthoughts.jikkou.rest.data.ResourceListRequest;
import io.streamthoughts.jikkou.rest.data.ResourceReconcileRequest;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import io.streamthoughts.jikkou.rest.services.ApiResourceService;
import java.net.URI;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class ApiResourceTest {

    private ApiResourceService mockService;
    private ReconciliationContextAdapter mockAdapter;
    private ApiResource controller;
    private HttpRequest<?> mockRequest;
    private HttpParameters mockParameters;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        mockService = Mockito.mock(ApiResourceService.class);
        mockAdapter = Mockito.mock(ReconciliationContextAdapter.class);
        controller = new ApiResource(mockService, mockAdapter);
        mockRequest = Mockito.mock(HttpRequest.class);
        mockParameters = Mockito.mock(HttpParameters.class);
        Mockito.when(mockRequest.getUri()).thenReturn(URI.create("/apis/group/v1/resources"));
        Mockito.when(mockParameters.names()).thenReturn(Collections.emptySet());
    }

    @Test
    void shouldReturnOkWithResourceListWhenListCalled() {
        // Given
        ResourceList<HasMetadata> expected = ResourceList.empty();
        Mockito.when(mockAdapter.getListContext(Mockito.any(ResourceListRequest.class)))
                .thenReturn(ListContext.Default.EMPTY);
        Mockito.when(mockService.search(Mockito.any(ApiResourceIdentifier.class), Mockito.any(ListContext.class)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.list(mockRequest, "group", "v1", "resources", mockParameters);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.body());
    }

    @Test
    void shouldDelegateToServiceSearchWhenListCalled() {
        // Given
        ResourceList<HasMetadata> expected = ResourceList.empty();
        Mockito.when(mockAdapter.getListContext(Mockito.any(ResourceListRequest.class)))
                .thenReturn(ListContext.Default.EMPTY);
        Mockito.when(mockService.search(Mockito.any(ApiResourceIdentifier.class), Mockito.any(ListContext.class)))
                .thenReturn(expected);

        // When
        controller.list(mockRequest, "kafka.jikkou.io", "v1", "kafkatopics", mockParameters);

        // Then
        ArgumentCaptor<ApiResourceIdentifier> captor = ArgumentCaptor.forClass(ApiResourceIdentifier.class);
        Mockito.verify(mockService).search(captor.capture(), Mockito.any(ListContext.class));
        ApiResourceIdentifier identifier = captor.getValue();
        Assertions.assertEquals("kafka.jikkou.io", identifier.group());
        Assertions.assertEquals("v1", identifier.version());
        Assertions.assertEquals("kafkatopics", identifier.plural());
    }

    @Test
    void shouldReturnOkWithSingleResourceWhenGetCalled() {
        // Given
        HasMetadata expected = new GenericResource("group/v1", "Resource", new ObjectMeta(), null);
        Mockito.when(mockService.get(Mockito.any(ApiResourceIdentifier.class), Mockito.eq("my-resource"), Mockito.any(Configuration.class)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.get(mockRequest, "group", "v1", "resources", "my-resource", mockParameters);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.body());
    }

    @Test
    void shouldReturnOkWithResourceListWhenSelectCalled() {
        // Given
        ResourceList<HasMetadata> expected = ResourceList.empty();
        ResourceListRequest payload = new ResourceListRequest();
        Mockito.when(mockAdapter.getListContext(Mockito.any(ResourceListRequest.class)))
                .thenReturn(ListContext.Default.EMPTY);
        Mockito.when(mockService.search(Mockito.any(ApiResourceIdentifier.class), Mockito.any(ListContext.class)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.select(mockRequest, "group", "v1", "resources", mockParameters, payload);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Mockito.verify(mockService).search(Mockito.any(ApiResourceIdentifier.class), Mockito.any(ListContext.class));
    }

    @Test
    void shouldReturnOkWithValidationResultWhenValidateCalled() {
        // Given
        ResourceReconcileRequest requestBody = new ResourceReconcileRequest();
        ResourceList<HasMetadata> expected = ResourceList.empty();
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        Mockito.when(mockAdapter.getReconciliationContext(requestBody, true)).thenReturn(context);
        Mockito.when(mockService.validate(Mockito.any(ApiResourceIdentifier.class), Mockito.anyList(), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.validate(mockRequest, "group", "v1", "resources", requestBody);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Mockito.verify(mockAdapter).getReconciliationContext(requestBody, true);
    }

    @Test
    void shouldReturnOkWithDiffResultWhenDiffCalled() {
        // Given
        ResourceReconcileRequest requestBody = new ResourceReconcileRequest();
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        ApiResourceChangeList expected = Mockito.mock(ApiResourceChangeList.class);
        Mockito.when(mockAdapter.getReconciliationContext(requestBody, true)).thenReturn(context);
        Mockito.when(mockService.diff(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.diff(mockRequest, "group", "v1", "resources", "", "", requestBody);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Mockito.verify(mockAdapter).getReconciliationContext(requestBody, true);
    }

    @Test
    void shouldReturnOkWithReconcileResultWhenReconcileCalled() {
        // Given
        ResourceReconcileRequest requestBody = new ResourceReconcileRequest();
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        ApiChangeResultList expected = Mockito.mock(ApiChangeResultList.class);
        Mockito.when(mockAdapter.getReconciliationContext(requestBody, false)).thenReturn(context);
        Mockito.when(mockService.reconcile(Mockito.any(), Mockito.eq(ReconciliationMode.CREATE), Mockito.anyList(), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.reconcile("group", "v1", "resources", ReconciliationMode.CREATE, false, requestBody);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Mockito.verify(mockService).reconcile(Mockito.any(), Mockito.eq(ReconciliationMode.CREATE), Mockito.anyList(), Mockito.eq(context));
    }

    @Test
    void shouldPassDryRunTrueWhenReconcileWithDryRun() {
        // Given
        ResourceReconcileRequest requestBody = new ResourceReconcileRequest();
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        Mockito.when(mockAdapter.getReconciliationContext(requestBody, true)).thenReturn(context);
        Mockito.when(mockService.reconcile(Mockito.any(), Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(Mockito.mock(ApiChangeResultList.class));

        // When
        controller.reconcile("group", "v1", "resources", ReconciliationMode.CREATE, true, requestBody);

        // Then
        Mockito.verify(mockAdapter).getReconciliationContext(requestBody, true);
    }

    @Test
    void shouldPassDryRunFalseWhenReconcileWithoutDryRun() {
        // Given
        ResourceReconcileRequest requestBody = new ResourceReconcileRequest();
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        Mockito.when(mockAdapter.getReconciliationContext(requestBody, false)).thenReturn(context);
        Mockito.when(mockService.reconcile(Mockito.any(), Mockito.any(), Mockito.anyList(), Mockito.any()))
                .thenReturn(Mockito.mock(ApiChangeResultList.class));

        // When
        controller.reconcile("group", "v1", "resources", ReconciliationMode.CREATE, false, requestBody);

        // Then
        Mockito.verify(mockAdapter).getReconciliationContext(requestBody, false);
    }

    @Test
    void shouldReturnOkWithPatchResultWhenPatchCalled() {
        // Given
        ResourceReconcileRequest requestBody = new ResourceReconcileRequest();
        ReconciliationContext context = ReconciliationContext.Default.EMPTY;
        ApiChangeResultList expected = Mockito.mock(ApiChangeResultList.class);
        Mockito.when(mockAdapter.getReconciliationContext(requestBody, false)).thenReturn(context);
        Mockito.when(mockService.patch(Mockito.eq(ReconciliationMode.CREATE), Mockito.anyList(), Mockito.eq(context)))
                .thenReturn(expected);

        // When
        HttpResponse<?> response = controller.patch(ReconciliationMode.CREATE, false, requestBody);

        // Then
        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Mockito.verify(mockService).patch(Mockito.eq(ReconciliationMode.CREATE), Mockito.anyList(), Mockito.eq(context));
    }
}
