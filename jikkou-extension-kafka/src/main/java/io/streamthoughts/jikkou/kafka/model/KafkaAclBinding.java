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
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.streamthoughts.jikkou.api.model.Nameable;
import java.io.Serializable;
import java.util.Objects;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

public class KafkaAclBinding implements Serializable, Nameable {

    private final String principalType;

    private final String principalName;

    private final String resourcePattern;

    private final PatternType patternType;

    private final ResourceType resourceType;

    private final AclOperation operation;

    private final AclPermissionType type;

    private final String host;

    /**
     * Flag to indicate this binding should be deleted.
     */
    private  boolean delete;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class to create a new {@link KafkaAclBinding} object.
     */
    public static class Builder {

        private static final String ACL_PRINCIPAL_TYPE_SEPARATOR = ":";

        private String principalType;
        private String principalName;
        private String resource;
        private PatternType patternType = PatternType.LITERAL;
        private ResourceType resourceType;
        private AclPermissionType type;
        private AclOperation operation;
        private String host;

        private boolean delete = false;

        public Builder withDelete(final boolean delete) {
            this.delete = delete;
            return this;
        }

        public Builder withPrincipal(final String principal) {
            String[] parts = principal.split(ACL_PRINCIPAL_TYPE_SEPARATOR);
            return this
                    .withPrincipalType(parts[0])
                    .withPrincipalName(parts[1]);
        }

        public Builder withPrincipalType(final String principalType) {
            this.principalType = principalType;
            return this;
        }

        public Builder withPrincipalName(final String principalName) {
            this.principalName = principalName;
            return this;
        }

        public Builder withResourcePattern(final String resource) {
            this.resource = resource;
            return this;
        }

        public Builder withPatternType(final PatternType patternType) {
            this.patternType = patternType;
            return this;
        }

        public Builder withResourceType(final ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder withType(final AclPermissionType permission) {
            this.type = permission;
            return this;
        }

        public Builder withOperation(final AclOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder withHost(final String host) {
            this.host = host;
            return this;
        }

        public KafkaAclBinding build() {
            return new KafkaAclBinding(
                    principalType,
                    principalName,
                    resource,
                    patternType,
                    resourceType,
                    type,
                    operation,
                    host,
                    delete
            );
        }
    }

    /**
     * Creates a new {@link KafkaAclBinding} instance.
     */
    KafkaAclBinding(@NotNull final String principalType,
                    @NotNull final String principalName,
                    @NotNull final String resourcePattern,
                    @NotNull final PatternType patternType,
                    @NotNull final ResourceType resourceType,
                    @NotNull final AclPermissionType type,
                    @NotNull final AclOperation operation,
                    @NotNull final String host,
                    final boolean delete) {
        this.principalType = Objects.requireNonNull(principalType, "'principalType' must not be null");
        this.principalName = Objects.requireNonNull(principalName, "'principalName' must not be null");
        this.type = Objects.requireNonNull(type, "'type' must not be null");
        this.operation = Objects.requireNonNull(operation, "'operation' must not be null");
        this.host = Objects.requireNonNull(host, "'host' must not be null");
        this.resourcePattern = Objects.requireNonNull(resourcePattern, "'resourcePattern' must not be null");
        this.patternType = Objects.requireNonNull(patternType, "'patternType' must not be null");
        this.resourceType = Objects.requireNonNull(resourceType, "'resourceType' must not be null");
        this.delete = delete;
    }

    @JsonIgnore
    public String getPrincipalName() {
        return principalName;
    }

    @JsonIgnore
    public String getPrincipalType() {
        return principalType;
    }

    public void isDelete(boolean delete) {
        this.delete = delete;
    }
    @JsonIgnore
    public boolean isDelete() {
        return delete;
    }

    public AclPermissionType getType() {
        return type;
    }

    public String getPrincipal() {
        return this.principalType + ":" + this.principalName;
    }

    public String getResourcePattern() {
        return resourcePattern;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public AclOperation getOperation() {
        return operation;
    }

    public String getHost() {
        return host;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaAclBinding that = (KafkaAclBinding) o;
        return Objects.equals(principalType, that.principalType) &&
               Objects.equals(principalName, that.principalName) &&
               Objects.equals(resourcePattern, that.resourcePattern) &&
                patternType == that.patternType &&
                resourceType == that.resourceType &&
                operation == that.operation &&
                type == that.type &&
                Objects.equals(host, that.host);
    }

    /** {@inheritDoc} **/
    @Override
    public int hashCode() {
        return Objects.hash(
                principalType,
                principalName,
                resourcePattern,
                patternType,
                resourceType,
                operation,
                type,
                host
        );
    }

    @Override
    public String toString() {
        return "KafkaAclBinding{" +
                "principalType='" + principalType + '\'' +
                ", principalName='" + principalName + '\'' +
                ", resourcePattern='" + resourcePattern + '\'' +
                ", patternType=" + patternType +
                ", resourceType=" + resourceType +
                ", operation=" + operation +
                ", permission=" + type +
                ", host='" + host + '\'' +
                '}';
    }

    /** {@inheritDoc} **/
    @Override
    @JsonIgnore
    public String getName() {
        return principalName;
    }
}
