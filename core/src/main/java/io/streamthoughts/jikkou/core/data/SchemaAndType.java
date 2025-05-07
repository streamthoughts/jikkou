/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.data;

import com.fasterxml.jackson.annotation.JsonValue;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

@Reflectable
public class SchemaAndType {

    private static final SchemaAndType EMPTY = new SchemaAndType();

    public static SchemaAndType empty() {
        return EMPTY;
    }

    @JsonValue
    private final String schema;
    private final SchemaType type;
    private final boolean useCanonicalFingerPrint;

    /**
     * Creates a new {@link SchemaAndType} instance.
     */
    private SchemaAndType() {
        this.schema = null;
        this.type = null;
        this.useCanonicalFingerPrint = false;
    }

    /**
     * Creates a new {@link SchemaAndType} instance.
     *
     * @param schema the schema string.
     * @param type   the schema type.
     */
    public SchemaAndType(@NotNull String schema,
                         @NotNull SchemaType type) {
        this.schema = Objects.requireNonNull(schema, "schema must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.useCanonicalFingerPrint = false;
    }

    /**
     * Creates a new {@link SchemaAndType} instance.
     *
     * @param schema the schema string.
     * @param type   the schema type.
     */
    public SchemaAndType(@NotNull String schema,
                         @NotNull SchemaType type,
                         boolean useCanonicalFingerPrint) {
        this.schema = Objects.requireNonNull(schema, "schema must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.useCanonicalFingerPrint = useCanonicalFingerPrint;
    }

    @JsonValue
    public String schema() {
        return schema;
    }

    public SchemaType type() {
        return type;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaAndType that = (SchemaAndType) o;
        return Objects.equals(
            this.type != null ? this.type.comparableSchemaForm(this.schema, this.useCanonicalFingerPrint) : null,
            that.type != null ? that.type.comparableSchemaForm(that.schema, that.useCanonicalFingerPrint) : null
        );
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(schema, type);
    }

    /**
     * {@inheritDoc}
     **/
    public String toString() {
        return schema;
    }
}
