/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.api.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.Objects;

/**
 * Confluent Cloud IAM v2 Role Binding.
 *
 * @param id         Role binding ID (read-only, e.g. {@code rb-f3a90de}).
 * @param principal  Principal (pattern: {@code User:*} or {@code Group:*}).
 * @param roleName   Role name (e.g. {@code CloudClusterAdmin}).
 * @param crnPattern CRN pattern (pattern: {@code crn://*}).
 */
@Reflectable
@JsonPropertyOrder({
    "id",
    "principal",
    "role_name",
    "crn_pattern"
})
public record RoleBindingData(
    @JsonProperty("id") String id,
    @JsonProperty("principal") String principal,
    @JsonProperty("role_name") String roleName,
    @JsonProperty("crn_pattern") String crnPattern
) {

    /**
     * Creates a new {@link RoleBindingData} without an ID (for create requests).
     */
    public RoleBindingData(String principal, String roleName, String crnPattern) {
        this(null, principal, roleName, crnPattern);
    }

    /**
     * {@inheritDoc}
     * <p>Equality is based on the triple (principal, roleName, crnPattern), not the id.</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleBindingData that = (RoleBindingData) o;
        return Objects.equals(principal, that.principal) &&
            Objects.equals(roleName, that.roleName) &&
            Objects.equals(crnPattern, that.crnPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(principal, roleName, crnPattern);
    }
}
