/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Lists Schema Registry subjects - HTTP-response
 *
 * @param subjects List of Kafka Schema Subjects.
 * @param errors   List of errors occurred during request processing
 * @param message  Printable result of the request
 */
@Reflectable
public record ListSchemaSubjectsResponse(List<String> subjects, List<Error> errors, String message) {

    @ConstructorProperties({
            "subjects",
            "errors",
            "message",
    })
    public ListSchemaSubjectsResponse { }

    /**
     * {@inheritDoc}
     **/
    @Override
    public List<Error> errors() {
        return Optional.ofNullable(errors).orElseGet(Collections::emptyList);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public String toString() {
        return "ListKafkaSchemaSubjects {" +
                "subjects=" + subjects +
                ", errors=" + errors +
                ", message=" + message +
                '}';
    }
}
