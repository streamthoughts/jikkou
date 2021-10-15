/*
 * Copyright 2020 StreamThoughts.
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
package io.streamthoughts.kafka.specs.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.kafka.common.acl.AclOperation;

import java.io.Serializable;
import java.util.Objects;

public class V1AccessOperationPolicy implements Serializable {

    private static final String ANY_HOSTS = "*";

    private final String host;
    private final AclOperation operation;

    public static V1AccessOperationPolicy fromString(final String policy) {
        if (policy.contains(":")) {
            String operation = policy.substring(0, policy.indexOf(":"));
            String host = policy.substring(operation.length() + 1);
            return new V1AccessOperationPolicy(AclOperation.fromString(operation), host);
        }

        return new V1AccessOperationPolicy(AclOperation.fromString(policy));
    }

    @JsonCreator
    public V1AccessOperationPolicy(final String value) {
        if (value.contains(":")) {
            String operation = value.substring(0, value.indexOf(":"));
            String host = value.substring(operation.length() + 1);
            this.operation = AclOperation.fromString(operation);
            this.host = host;
        } else {
            this.operation = AclOperation.fromString(value);
            this.host = ANY_HOSTS;
        }
    }

    /**
     * Creates a new {@link V1AccessOperationPolicy} instance.
     *
     * @param operation the {@link AclOperation}.
     */
    public V1AccessOperationPolicy(final AclOperation operation) {
        this(operation, ANY_HOSTS);
    }

    /**
     * Creates a new {@link V1AccessOperationPolicy} instance.
     *
     * @param operation the {@link AclOperation}.
     * @param host      the host.
     */
    public V1AccessOperationPolicy(final AclOperation operation, final String host) {
        Objects.requireNonNull(host, "host should be non-null");
        Objects.requireNonNull(operation, "operation should be non-null");
        this.host = host;
        this.operation = operation;
    }

    public String host() {
        return host;
    }

    public AclOperation operation() {
        return operation;
    }

    public String toLiteral() {
        return operation.name() + ":" + host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V1AccessOperationPolicy that = (V1AccessOperationPolicy) o;
        return Objects.equals(host, that.host) &&
                operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, operation);
    }

    @JsonValue
    @Override
    public String toString() {
        return operation + ":" + host;
    }
}
