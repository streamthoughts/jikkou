/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.repository;

import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionContext;
import io.streamthoughts.jikkou.core.extension.DefaultExtensionDescriptorFactory;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorModifiers;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LocalResourceRepositoryTest {

    public static final ExtensionDescriptor<LocalResourceRepository> DESCRIPTOR =
        new DefaultExtensionDescriptorFactory().make(
            LocalResourceRepository.class,
            LocalResourceRepository::new
        );

    @Test
    void shouldGetEmptyListGivenNoFile() {
        // Given
        LocalResourceRepository repository = new LocalResourceRepository();
        repository.init(new DefaultExtensionContext(null, DESCRIPTOR, null));

        // When
        List<? extends HasMetadata> results = repository.all();

        // Then
        Assertions.assertEquals(results, List.of());
    }

    @Test
    void shouldLoadResourcesFromLocalFiles() {
        // Given
        ExtensionDescriptor<LocalResourceRepository> descriptor = ExtensionDescriptorModifiers.withConfiguration(
            Configuration.from(Map.of(
                LocalResourceRepository.Config.FILES_CONFIG.key(), List.of("classpath://test-resource.yaml"),
                LocalResourceRepository.Config.VALUE_FILES_CONFIG.key(), List.of("classpath://test-values.yaml"))
            )
        ).apply(DESCRIPTOR);

        LocalResourceRepository repository = new LocalResourceRepository();
        repository.init(new DefaultExtensionContext(null, descriptor, null));

        // When
        List<? extends HasMetadata> results = repository.all();

        // Then
        GenericResource resource = (GenericResource) results.getFirst();
        Assertions.assertEquals(
            Map.of("spec", Map.of("values", Map.of("key1", "value1",
                "key2", "value2",
                "key3", "value3",
                "key4", "value4")
            )),
            resource.getAdditionalProperties()
        );
    }
}