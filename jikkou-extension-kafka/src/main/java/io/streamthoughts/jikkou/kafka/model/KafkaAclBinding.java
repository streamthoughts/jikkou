/*
 * Copyright 2020 The original authors
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
package io.streamthoughts.jikkou.kafka.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.streamthoughts.jikkou.annotation.Reflectable;
import java.util.Objects;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a binding between a resource pattern and an access control entry.
 *
 * @param principal       the principal to grant or deny permissions.
 * @param resourcePattern the Kafka resource pattern. Cannot be {@code null}.
 * @param patternType     the Kafka ACL binding pattern type. Cannot be {@code null}.
 * @param resourceType    the kafka resource type. Cannot be {@code null}.
 * @param operation       the ACL operation. Cannot be {@code null}.
 * @param type            represents whether an ACL grants or denies permissions. Cannot be {@code null}.
 * @param host            the host. Cannot be {@code null}.
 * @param isDeleted       specify if this binding should be deleted.
 */
@Reflectable
public record KafkaAclBinding(
        @JsonProperty @NotNull String principal,

        @JsonProperty @NotNull String resourcePattern,

        @JsonProperty @NotNull PatternType patternType,

        @JsonProperty @NotNull ResourceType resourceType,

        @JsonProperty @NotNull AclOperation operation,

        @JsonProperty @NotNull AclPermissionType type,

        @JsonProperty @NotNull String host,

        @JsonIgnore boolean isDeleted

) {
    public KafkaAclBinding(@NotNull String principal,
                           @NotNull String resourcePattern,
                           @NotNull PatternType patternType,
                           @NotNull ResourceType resourceType,
                           @NotNull AclOperation operation,
                           @NotNull AclPermissionType type,
                           @NotNull String host) {
        this(principal, resourcePattern, patternType, resourceType, operation, type, host, false);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaAclBinding that = (KafkaAclBinding) o;
        return Objects.equals(principal, that.principal) &&
                Objects.equals(resourcePattern, that.resourcePattern) &&
                patternType == that.patternType &&
                resourceType == that.resourceType &&
                operation == that.operation &&
                type == that.type &&
                Objects.equals(host, that.host);
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int hashCode() {
        return Objects.hash(
                principal,
                resourcePattern,
                patternType,
                resourceType,
                operation,
                type,
                host
        );
    }
}
