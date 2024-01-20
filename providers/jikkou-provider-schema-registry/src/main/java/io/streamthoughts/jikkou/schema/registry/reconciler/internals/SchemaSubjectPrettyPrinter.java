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
package io.streamthoughts.jikkou.schema.registry.reconciler.internals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.streamthoughts.jikkou.schema.registry.model.SchemaHandle;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
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
