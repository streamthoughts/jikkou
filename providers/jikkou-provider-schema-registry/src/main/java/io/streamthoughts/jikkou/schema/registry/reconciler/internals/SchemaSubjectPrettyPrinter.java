/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.reconciler.internals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.core.data.SchemaHandle;
import io.streamthoughts.jikkou.core.data.SchemaType;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubject;
import io.streamthoughts.jikkou.schema.registry.models.V1SchemaRegistrySubjectSpec;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchemaSubjectPrettyPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaSubjectPrettyPrinter.class);

    @NotNull
    public static V1SchemaRegistrySubject prettyPrintSchema(@NotNull V1SchemaRegistrySubject resource) {
        V1SchemaRegistrySubjectSpec spec = resource.getSpec();
        SchemaType type = spec.getSchemaType();
        if (type == SchemaType.AVRO || type == SchemaType.JSON) {
            SchemaHandle schema = spec.getSchema();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode tree = objectMapper.readTree(schema.value());
                String pretty = objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(tree);
                spec.setSchema(new SchemaHandle(pretty));

            } catch (JsonProcessingException e) {
                LOG.warn("Failed to parse AVRO or JSON schema", e);
            }
        }
        return resource;
    }
}
