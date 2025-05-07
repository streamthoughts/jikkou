/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.data.SchemaType;
import java.util.Collections;
import java.util.List;

@Reflectable
public record SubjectSchemaRegistration(
    @JsonProperty("schema") String schema,
    @JsonProperty("schemaType") SchemaType schemaType,
    @JsonProperty("references") List<SubjectSchemaReference> references
) {

    /**
     * Creates a new {@link SubjectSchemaRegistration} instance.
     *
     * @param schema     subject under which the schema will be registered.
     * @param schemaType the schema format.
     */
    public SubjectSchemaRegistration(String schema,
                                     SchemaType schemaType) {
        this(schema, schemaType, Collections.emptyList());
    }
}
