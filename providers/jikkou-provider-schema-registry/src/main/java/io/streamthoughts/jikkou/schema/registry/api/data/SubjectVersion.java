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

/**
 * A subject-version pairs.
 */
@Reflectable
public final class SubjectVersion {

    private final String subject;
    private final int version;

    /**
     * Creates a new {@link SubjectVersion} instance.
     * @param subject   the name of the subject.
     * @param version   the version of the schema.
     */
    @JsonCreator
    public SubjectVersion(@JsonProperty("subject") String subject, @JsonProperty("version") int version) {
        this.subject = subject;
        this.version = version;
    }

    /**
     * Gets the name of the subject.
     * @return string name.
     */
    public String subject() {
        return subject;
    }

    /**
     * Gets the version of the schema.
     *
     * @return integer version.
     */
    public int  version() {
        return version;
    }
    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectVersion that = (SubjectVersion) o;
        return version == that.version && Objects.equals(subject, that.subject);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(subject, version);
    }
    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "[" +
                "subject=" + subject +
                ", version=" + version +
                ']';
    }
}
