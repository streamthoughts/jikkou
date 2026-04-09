/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.iceberg.internals;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jikkou.iceberg.table.models.V1IcebergSchema;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * Custom Jackson deserializer for {@link V1IcebergSchema} that handles both:
 * <ul>
 *   <li>Inline schemas: {@code { "columns": [...] }}</li>
 *   <li>Referenced schemas: {@code { "$ref": "file://path/to/schema.yaml" }}</li> (TODO: implement $ref loading)
 * </ul>
 */
public final class IcebergSchemaHandle extends JsonDeserializer<V1IcebergSchema> {

    private static final String REF_FIELD = "$ref";

    /** {@inheritDoc} */
    @Override
    public V1IcebergSchema deserialize(@NotNull final JsonParser parser,
                                       @NotNull final DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);

        if (node.has(REF_FIELD)) {
            // TODO: implement $ref loading — load file content, parse as V1IcebergSchema
            // For now, return an empty schema to avoid breaking deserialization
            String ref = node.get(REF_FIELD).asText();
            throw new UnsupportedOperationException(
                "Schema $ref is not yet supported. Found: $ref=" + ref +
                ". Please inline the schema definition in the resource spec.");
        }

        // Inline schema — deserialize normally
        return mapper.treeToValue(node, V1IcebergSchema.class);
    }
}
