/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.resource;

import io.streamthoughts.jikkou.core.annotation.ApiVersion;
import io.streamthoughts.jikkou.core.annotation.Description;
import io.streamthoughts.jikkou.core.annotation.Kind;
import io.streamthoughts.jikkou.core.annotation.Names;
import io.streamthoughts.jikkou.core.extension.exceptions.NoSuchExtensionException;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import io.streamthoughts.jikkou.core.resource.exception.ConflictingResourceDefinitionException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultResourceRegistryTest {

    static final ResourceDescriptor DESCRIPTOR = new ResourceDescriptor(
            ResourceType.of(TestResource.class),
            "Test description",
            TestResource.class,
            "test",
            "tests",
            Set.of("t", "ts"),
            Set.of(Verb.LIST),
            false
    );

    private DefaultResourceRegistry registry;

    @BeforeEach
    void beforeEach() {
        this.registry = new DefaultResourceRegistry();
    }


    @Test
    void shouldReturnDescriptorAfterRegisteringResource() {
        ResourceDescriptor descriptor = registry.register(TestResource.class);
        Assertions.assertEquals(DESCRIPTOR, descriptor);
    }

    @Test
    void shouldThrowConflictingExtensionDefinitionExceptionForDuplicateResource() {
        registry.register(TestResource.class);
        Assertions.assertThrows(
                ConflictingResourceDefinitionException.class,
                () -> registry.register(TestResource.class)
        );
    }

    @Test
    void shouldGetDescriptorByTypeForRegisteredResource() {
        registry.register(TestResource.class);
        ResourceDescriptor descriptor = registry.getDescriptorByType(ResourceType.of(TestResource.class));
        Assertions.assertEquals(DESCRIPTOR, descriptor);
    }

    @Test
    void shouldThrowNoSuchExtensionExceptionForNotRegisteredResource() {
        Assertions.assertThrows(
                NoSuchExtensionException.class,
                () -> registry.getDescriptorByType(ResourceType.of(TestResource.class)));
    }

    @Test
    void shouldGetAllDescriptors() {
        registry.register(TestResource.class);
        List<ResourceDescriptor> descriptors = registry.allDescriptors();
        Assertions.assertEquals(List.of(DESCRIPTOR), descriptors);
    }

    @Test
    void shouldGetDescriptorForKindCaseSensitiveFalse() {
        registry.register(TestResource.class);
        Optional<ResourceDescriptor> optional = registry.findDescriptorByType(
                "test",
                "test.jikkou.io",
                "v1beta2",
                false);
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(DESCRIPTOR, optional.get());
    }

    @Test
    void shouldGetDescriptorForKindCaseSensitiveTrue() {
        registry.register(TestResource.class);
        Optional<ResourceDescriptor> optional = registry.findDescriptorByType(
                "test",
                "test.jikkou.io",
                "v1beta2",
                true);
        Assertions.assertTrue(optional.isEmpty());
    }

    @Test
    void shouldGetDescriptorForType() {
        registry.register(TestResource.class);
        Optional<ResourceDescriptor> optional = registry.findDescriptorByType(ResourceType.of(TestResource.class));
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals(DESCRIPTOR, optional.get());
    }

    @ApiVersion("test.jikkou.io/v1beta2")
    @Kind("Test")
    @Description("Test description")
    @Names(
            plural = "tests",
            singular = "test",
            shortNames = {"t", "ts"}
    )
    static abstract class TestResource implements HasMetadata {
    }

    ;
}