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