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
package io.streamthoughts.jikkou.extension.aiven.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class KafkaAclEntriesResponse {

    /**
     * List of Kafka ACL entries.
     */
    private final List<KafkaAclEntry> acl;

    /**
     * List of errors occurred during request processing
     */
    private final List<Error> errors;

    /**
     * Printable result of the request
     */
    private final String message;

    @JsonCreator
    public KafkaAclEntriesResponse(@JsonProperty("acl") List<KafkaAclEntry> acl,
                                   @JsonProperty("errors") List<Error> errors,
                                   @JsonProperty("message") String message) {
        this.acl = acl;
        this.errors = errors;
        this.message = message;
    }

    public List<KafkaAclEntry> acl() {
        return acl;
    }

    public List<Error> errors() {
        return Optional.ofNullable(errors).orElseGet(Collections::emptyList);
    }

    public String message() {
        return message;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaAclEntriesResponse that = (KafkaAclEntriesResponse) o;
        return Objects.equals(acl, that.acl) && Objects.equals(errors, that.errors) && Objects.equals(message, that.message);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(acl, errors, message);
    }

    /** {@inheritDoc} **/
    @Override
    public String toString() {
        return "KafkaAclEntriesResponse{" +
                "acl=" + acl +
                ", errors=" + errors +
                ", message='" + message + '\'' +
                '}';
    }
}
