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
package io.streamthoughts.kafka.specs.acl;

import org.apache.kafka.common.acl.AclOperation;

import java.util.Objects;

public class AclOperationPolicy {

    private static final String ANY_HOSTS = "*";

    private final String host;
    private final AclOperation operation;

    public static AclOperationPolicy fromString(final String policy) {
        if (policy.contains(":")) {
            String operation = policy.substring(0, policy.indexOf(":"));
            String host = policy.substring(operation.length() + 1, policy.length());
            return new AclOperationPolicy(AclOperation.fromString(operation), host);
        }

        return new AclOperationPolicy(AclOperation.fromString(policy));
    }

    /**
     * Creates a new {@link AclOperationPolicy} instance.
     * @param operation
     */
    public AclOperationPolicy(final AclOperation operation) {
        this(operation, ANY_HOSTS);
    }

    /**
     * Creates a new {@link AclOperationPolicy} instance.
     * @param host
     * @param operation
     */
    public AclOperationPolicy(final AclOperation operation, final String host) {
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
        AclOperationPolicy that = (AclOperationPolicy) o;
        return Objects.equals(host, that.host) &&
                operation == that.operation;
    }

    @Override
    public int hashCode() {

        return Objects.hash(host, operation);
    }

    @Override
    public String toString() {
        return "AclOperationPolicy{" +
                "host='" + host + '\'' +
                ", operation=" + operation +
                '}';
    }
}
