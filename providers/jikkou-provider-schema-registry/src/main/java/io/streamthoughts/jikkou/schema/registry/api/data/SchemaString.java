/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Objects;

@Reflectable
public final class SchemaString {

    private final String schema;

    /**
     * Creates a new {@link SchemaString} instance.
     *
     * @param schema a schema string.
     */
    @JsonCreator
    public SchemaString(@JsonProperty("schema") String schema) {
        this.schema = schema;
    }

    /**
     * Gets a schema string.
     *
     * @return a schema string.
     */
    public String schema() {
        return schema;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaString that = (SchemaString) o;
        return Objects.equals(schema, that.schema);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(schema);
    }

    /** {@inheritDoc} **/
    @Override
    public String
    toString() {
        return "{schema=" + schema +  '}';
    }
}
