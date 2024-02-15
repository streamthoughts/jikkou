/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.services;

import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.DefaultResourceRegistry;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.rest.exception.ApiResourceNotFoundException;
import io.streamthoughts.jikkou.rest.models.ApiResourceIdentifier;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DefaultApiResourceServiceTest {

    @Test
    void shouldReturnNonNullForGetDescriptorByIdentifier() {
        // Given
        JikkouApi mkApi = Mockito.mock(JikkouApi.class);
        DefaultResourceRegistry registry = new DefaultResourceRegistry();
        ResourceDescriptor descriptor = new ResourceDescriptor(
                new ResourceType("Resource", "group", "1"),
                "",
                HasMetadata.class,
                "singularName",
                "pluralName",
                Collections.emptySet(),
                Collections.emptySet(),
                false
        );
        registry.register(descriptor);
        DefaultApiResourceService service = new DefaultApiResourceService(mkApi, registry);

        // When
        ApiResourceIdentifier identifier = new ApiResourceIdentifier("group", "1", "pluralName");
        ResourceDescriptor result = service.getResourceDescriptorByIdentifier(identifier);

        // Then
        Assertions.assertEquals(descriptor, result);
    }

    @Test
    void shouldThrowExceptionForGetDescriptorByIdentifierWhenResourceNotFound() {
        // Given
        JikkouApi mkApi = Mockito.mock(JikkouApi.class);
        DefaultResourceRegistry registry = new DefaultResourceRegistry();;
        DefaultApiResourceService service = new DefaultApiResourceService(mkApi, registry);

        // When
        ApiResourceIdentifier identifier = new ApiResourceIdentifier("???", "???", "???");
        ApiResourceNotFoundException ex = Assertions.assertThrows(ApiResourceNotFoundException.class, () -> {
            // Then
            service.getResourceDescriptorByIdentifier(identifier);
        });
        Assertions.assertEquals(ex.identifier(), identifier);
    }
}