/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.api.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.common.utils.Classes;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.io.reader.ResourceReaderOptions;
import io.streamthoughts.jikkou.core.io.reader.TemplateResourceReader;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.NamedValueSet;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JinjaResourceTemplateRendererTest {

    private static final ObjectMapper OBJECT_MAPPER = Jackson.YAML_OBJECT_MAPPER;

    @Test
    @SuppressWarnings("unchecked")
    void should_load_template_resource_given_values_file() throws IOException {

        // Given
        ClassLoader classLoader = Classes.getClassLoader();
        try(
            InputStream template = classLoader.getResourceAsStream("datasets/resource-template.yaml");
            InputStream values = classLoader.getResourceAsStream("datasets/resource-values.yaml");
        ) {
            Map<String, Object> mapValues = OBJECT_MAPPER.readValue(values, Map.class);
            ResourceReaderOptions options = new ResourceReaderOptions()
                .withValues(NamedValueSet.setOf(mapValues));

            try (var reader = new TemplateResourceReader(
                new JinjaResourceTemplateRenderer(),
                () -> template,
                OBJECT_MAPPER
            )) {

                // When
                List<HasMetadata> results = reader.readAllResources(options);

                // Then
                Assertions.assertNotNull(results);
                Assertions.assertEquals(1, results.size());
                GenericResource resource = (GenericResource) results.getFirst();
                Map<String, Object> spec = (LinkedHashMap) resource.getAdditionalProperties().get("spec");
                Object topics = spec.get("topics");
                Assertions.assertNotNull(topics);
                Assertions.assertEquals(5, ((List) topics).size());
            }
        }
    }
}