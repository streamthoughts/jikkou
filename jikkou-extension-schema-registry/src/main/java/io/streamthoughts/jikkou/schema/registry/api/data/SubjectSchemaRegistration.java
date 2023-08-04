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
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import io.streamthoughts.jikkou.schema.registry.model.SchemaType;
import java.util.Collections;
import java.util.List;

@Reflectable
public final class SubjectSchemaRegistration {

    private final String schema;
    private final SchemaType schemaType;
    private final List<SubjectSchemaReference> references;

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


    /**
     * Creates a new {@link SubjectSchemaRegistration} instance.
     *
     * @param schema     subject under which the schema will be registered.
     * @param schemaType the schema format.
     * @param references specifies the names of referenced schemas.
     */
    @JsonCreator
    public SubjectSchemaRegistration(@JsonProperty("schema") String schema,
                                     @JsonProperty("schemaType") SchemaType schemaType,
                                     @JsonProperty("references") List<SubjectSchemaReference> references) {
        this.schema = schema;
        this.schemaType = schemaType;
        this.references = references;
    }

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
