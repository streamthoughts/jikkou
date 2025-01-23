/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.schema.registry.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.core.annotation.Reflectable;

/**
 * A subject-version pairs.
 */
@Reflectable
public record SubjectVersion(
    @JsonProperty("subject") String subject,
    @JsonProperty("version") int version) {

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "[" +
            "subject=" + subject +
            ", version=" + version +
            ']';
    }
}
