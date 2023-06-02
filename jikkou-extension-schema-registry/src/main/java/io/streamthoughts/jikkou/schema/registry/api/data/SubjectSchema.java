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

public final class SubjectSchema {

    /**
     * Name of the subject that this schema is registered under
     */
    private final String subject;

    /**
     *  Globally unique identifier of the schema.
     */
    private final int id;

    /**
     * Version of the returned schema.
     */
    private final int version;

    /**
     *  The schema format: AVRO is the default (if no schema type is shown on the response, the type is AVRO), PROTOBUF, JSON
     */
    private final String schemaType;

    /**
     * The schema string
     */
    private final String schema;

    @JsonCreator
    public SubjectSchema(@JsonProperty("subject") String subject,
                         @JsonProperty("id") int id,
                         @JsonProperty("version") int version,
                         @JsonProperty("schemaType") String schemaType,
                         @JsonProperty("schema") String schema) {
        this.subject = subject;
        this.id = id;
        this.version = version;
        this.schemaType = schemaType;
        this.schema = schema;
    }

    public String subject() {
        return subject;
    }

    public int id() {
        return id;
    }

    public int version() {
        return version;
    }

    public String schemaType() {
        return schemaType;
    }

    public String schema() {
        return schema;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectSchema that = (SubjectSchema) o;
        return id == that.id && version == that.version && Objects.equals(subject, that.subject) && Objects.equals(schemaType, that.schemaType) && Objects.equals(schema, that.schema);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(subject, id, version, schemaType, schema);
    }

    @Override
    public String toString() {
        return "{" +
                "subject=" + subject +
                ", id=" + id +
                ", version=" + version +
                ", schemaType=" + schemaType +
                ", schema=" + schema +
                '}';
    }
}
