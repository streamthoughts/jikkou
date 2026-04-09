/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.core.resource;

import com.fasterxml.jackson.databind.JsonNode;
import io.jikkou.core.models.generics.GenericResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JacksonResourceSchemaGeneratorTest {

    private final JacksonResourceSchemaGenerator generator = new JacksonResourceSchemaGenerator();

    @Test
    void shouldGenerateSchemaForGenericResource() {
        JsonNode schema = generator.generate(GenericResource.class);

        Assertions.assertNotNull(schema);
        Assertions.assertTrue(schema.has("$schema"), "Schema should contain $schema");
        Assertions.assertTrue(schema.has("type"), "Schema should contain type");
        Assertions.assertEquals("object", schema.get("type").asText());
    }

    @Test
    void shouldContainExpectedProperties() {
        JsonNode schema = generator.generate(GenericResource.class);

        JsonNode properties = schema.get("properties");
        Assertions.assertNotNull(properties, "Schema should contain properties");
        Assertions.assertTrue(properties.has("apiVersion"), "Should have apiVersion property");
        Assertions.assertTrue(properties.has("kind"), "Should have kind property");
        Assertions.assertTrue(properties.has("metadata"), "Should have metadata property");
    }

    @Test
    void shouldUseDraft202012() {
        JsonNode schema = generator.generate(GenericResource.class);

        String schemaDialect = schema.get("$schema").asText();
        Assertions.assertTrue(schemaDialect.contains("2020-12"),
                "Schema should use Draft 2020-12, but was: " + schemaDialect);
    }

    @Test
    void shouldNotContainJavaTypeNamesInSchema() {
        JsonNode schema = generator.generate(GenericResource.class);
        String json = schema.toString();

        Assertions.assertFalse(json.contains("Map(String"),
                "Schema should not contain Java type names like Map(String,Object), but was: " + json);
    }

    @Test
    void shouldIncludeDescriptionsFromAnnotations() {
        JsonNode schema = generator.generate(GenericResource.class);
        JsonNode properties = schema.path("properties");

        JsonNode metadataDescription = properties.path("metadata").path("description");
        Assertions.assertFalse(metadataDescription.isMissingNode(),
                "metadata property should have a description");
        Assertions.assertEquals("Metadata attached to the resource.", metadataDescription.asText());

        JsonNode apiVersionDescription = properties.path("apiVersion").path("description");
        Assertions.assertFalse(apiVersionDescription.isMissingNode(),
                "apiVersion property should have a description");
        Assertions.assertEquals("ApiVersion attached to the resource.", apiVersionDescription.asText());

        JsonNode kindDescription = properties.path("kind").path("description");
        Assertions.assertFalse(kindDescription.isMissingNode(),
                "kind property should have a description");
        Assertions.assertEquals("Kind attached to the resource.", kindDescription.asText());
    }

    @Test
    void shouldInlineMapProperties() {
        JsonNode schema = generator.generate(GenericResource.class);

        JsonNode labels = schema.path("properties").path("metadata").path("properties").path("labels");
        Assertions.assertFalse(labels.isMissingNode(), "metadata.labels should exist");
        Assertions.assertEquals("object", labels.path("type").asText(),
                "labels should have type 'object' inline, not a $ref");
        Assertions.assertFalse(labels.has("$ref"),
                "labels should not use $ref");
    }
}
