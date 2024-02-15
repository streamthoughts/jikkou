/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Objects;

@Reflectable
@JsonPropertyOrder({"name", "subject", "version"})
public final class SubjectSchemaReference {

    private final String name;
    private final String subject;
    private final int version;

    @JsonCreator
    public SubjectSchemaReference(@JsonProperty("name") String name,
                                  @JsonProperty("subject") String subject,
                                  @JsonProperty("version") int version) {
        this.name = name;
        this.subject = subject;
        this.version = version;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    @JsonProperty("version")
    public int getVersion() {
        return version;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectSchemaReference that = (SubjectSchemaReference) o;
        return version == that.version && Objects.equals(name, that.name) && Objects.equals(subject, that.subject);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(name, subject, version);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "SubjectSchemaReference[" +
                "name=" + name +
                ", subject='" + subject +
                ", version=" + version +
                ']';
    }
}
