/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.model;

import com.fasterxml.jackson.annotation.JsonValue;
import io.streamthoughts.jikkou.core.data.json.Json;
import io.streamthoughts.jikkou.schema.registry.avro.AvroSchema;
import java.util.Objects;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.apache.avro.Schema;
import org.apache.avro.SchemaNormalization;
import org.jetbrains.annotations.NotNull;

@Builder
@Jacksonized
public class SchemaAndType {

    private static final SchemaAndType EMPTY = new SchemaAndType();

    public static SchemaAndType empty() {
        return EMPTY;
    }

    @JsonValue
    private final String schema;
    private final SchemaType type;

    /**
     * Creates a new {@link SchemaAndType} instance.
     */
    private SchemaAndType() {
        this.schema = null;
        this.type = null;
    }

    /**
     * Creates a new {@link SchemaAndType} instance.
     * @param schema    the schema string.
     * @param type      the schema type.
     */
    public SchemaAndType(@NotNull String schema,
                         @NotNull SchemaType type) {
        this.schema = Objects.requireNonNull(schema, "schema must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    @JsonValue
    public String schema() {
        return schema;
    }

    public SchemaType type() {
        return type;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaAndType that = (SchemaAndType) o;
        if (type == SchemaType.AVRO && type == that.type) {
            return avroEquals(that);
        } else if (type == SchemaType.JSON && type == that.type) {
            return jsonEquals(that);
        } else {
            return Objects.equals(schema, that.schema) && type == that.type;
        }
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(schema, type);
    }

    private boolean avroEquals(SchemaAndType that) {
        Schema thisSchema = new AvroSchema(schema).schema();
        Schema thatSchema = new AvroSchema(that.schema).schema();

        return SchemaNormalization.parsingFingerprint64(thisSchema) ==
                SchemaNormalization.parsingFingerprint64(thatSchema);
    }

    private boolean jsonEquals(SchemaAndType that) {
        return Objects.equals(Json.normalize(schema), Json.normalize(that.schema));
    }

    public String toString() {
        return schema;
    }
}
