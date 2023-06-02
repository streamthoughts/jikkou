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
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * A globally unique identifier of the schema
 */
public final class SubjectSchemaId {

    private final int id;

    /**
     * Creates a new {@link SubjectSchemaId} instance.
     *
     * @param id a schema's id
     */
    @JsonCreator
    public SubjectSchemaId(@JsonProperty("id") int id) {
        this.id = id;
    }

    /**
     * Gets a schema .
     *
     * @return a schema string.
     */
    public int id() {
        return id;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectSchemaId that = (SubjectSchemaId) o;
        return Objects.equals(id, that.id);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** {@inheritDoc} **/
    @Override
    public String
    toString() {
        return "{ id=" + id +  '}';
    }
}
