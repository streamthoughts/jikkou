/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.util.Collections;
import java.util.List;

@Reflectable
public final class SubjectSchemaRegistration {

    private final String id;
    private final String version;
    private final String schema;
    private final SchemaType schemaType;
    private final List<SubjectSchemaReference> references;

    /**
     * Creates a new {@link SubjectSchemaRegistration} instance.
     *
     * @param schema     subject under which the schema will be registered.
     * @param schemaType the schema format.
     */
    public SubjectSchemaRegistration(String id,
                                     String version,
                                     String schema,
                                     SchemaType schemaType) {
        this(id, version, schema, schemaType, Collections.emptyList());
    }

    /**
     * Creates a new {@link SubjectSchemaRegistration} instance.
     *
     * @param schema     subject under which the schema will be registered.
     * @param schemaType the schema format.
     * @param references specifies the names of referenced schemas.
     */
    @JsonCreator
    public SubjectSchemaRegistration(@JsonProperty("id") String id,
                                     @JsonProperty("version") String version,
                                     @JsonProperty("schema") String schema,
                                     @JsonProperty("schemaType") SchemaType schemaType,
                                     @JsonProperty("references") List<SubjectSchemaReference> references) {
        this.id = id;
        this.version = version;
        this.schema = schema;
        this.schemaType = schemaType;
        this.references = references;
    }

    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String id() { return id; }

    @JsonProperty("version")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String version() { return version; }

    @JsonProperty("schema")
    public String schema() {
        return schema;
    }

    @JsonProperty("schemaType")
    public SchemaType schemaType() {
        return schemaType;
    }

    @JsonProperty("references")
    public List<SubjectSchemaReference> references() {
        return references;
    }
}
