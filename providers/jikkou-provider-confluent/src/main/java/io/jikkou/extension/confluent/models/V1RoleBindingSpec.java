/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.Reflectable;
import java.beans.ConstructorProperties;
import java.util.Objects;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@JsonPropertyOrder({
    "principal",
    "roleName",
    "crnPattern"
})
@Jacksonized
@Reflectable
public class V1RoleBindingSpec {

    /**
     * The principal (pattern: {@code ^(User|Group):.+$}).
     * (Required)
     */
    @JsonProperty("principal")
    @JsonPropertyDescription("The principal (e.g. User:sa-abc123 or Group:my-group)")
    private String principal;

    /**
     * The role name (e.g. CloudClusterAdmin).
     * (Required)
     */
    @JsonProperty("roleName")
    @JsonPropertyDescription("The role name")
    private String roleName;

    /**
     * The CRN pattern (pattern: {@code ^crn://.+$}).
     * (Required)
     */
    @JsonProperty("crnPattern")
    @JsonPropertyDescription("The Confluent Resource Name pattern")
    private String crnPattern;

    public V1RoleBindingSpec() {
    }

    @ConstructorProperties({
        "principal",
        "roleName",
        "crnPattern"
    })
    public V1RoleBindingSpec(String principal, String roleName, String crnPattern) {
        this.principal = principal;
        this.roleName = roleName;
        this.crnPattern = crnPattern;
    }

    @JsonProperty("principal")
    public String getPrincipal() {
        return principal;
    }

    @JsonProperty("roleName")
    public String getRoleName() {
        return roleName;
    }

    @JsonProperty("crnPattern")
    public String getCrnPattern() {
        return crnPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1RoleBindingSpec rhs)) return false;
        return Objects.equals(principal, rhs.principal) &&
            Objects.equals(roleName, rhs.roleName) &&
            Objects.equals(crnPattern, rhs.crnPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, roleName, crnPattern);
    }

    @Override
    public String toString() {
        return "V1RoleBindingSpec[" +
            "principal=" + principal +
            ", roleName=" + roleName +
            ", crnPattern=" + crnPattern +
            ']';
    }
}
