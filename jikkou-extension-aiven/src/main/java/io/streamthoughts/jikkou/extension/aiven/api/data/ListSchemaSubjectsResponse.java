/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.extension.aiven.api.data;

import io.streamthoughts.jikkou.annotation.Reflectable;
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
