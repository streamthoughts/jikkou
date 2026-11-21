/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jikkou.core.annotation.Reflectable;
import io.jikkou.core.data.SchemaType;
import java.util.Collections;
import java.util.List;

@Reflectable
public record SubjectSchemaRegistration(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("id") Integer id,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("version") Integer version,
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
    public SubjectSchemaRegistration(Integer id,
                                     Integer version,
                                     String schema,
                                     SchemaType schemaType) {
        this(id, version, schema, schemaType, Collections.emptyList());
    }
}
