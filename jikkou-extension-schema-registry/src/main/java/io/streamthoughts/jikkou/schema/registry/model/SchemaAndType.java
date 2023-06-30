/*
 * Copyright 2023 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.schema.registry.model;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class SchemaAndType {

    private static final SchemaAndType EMPTY = new SchemaAndType();

    public static SchemaAndType empty() {
        return EMPTY;
    }

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
        return Objects.equals(schema, that.schema) && type == that.type;
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(schema, type);
    }
}
