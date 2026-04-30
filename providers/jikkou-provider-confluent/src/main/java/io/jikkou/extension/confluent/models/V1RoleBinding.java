/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.models;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.jikkou.core.annotation.ApiVersion;
import io.jikkou.core.annotation.Description;
import io.jikkou.core.annotation.Kind;
import io.jikkou.core.annotation.Names;
import io.jikkou.core.annotation.Reflectable;
import io.jikkou.core.annotation.Verbs;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.HasSpec;
import io.jikkou.core.models.ObjectMeta;
import io.jikkou.core.models.Resource;
import io.jikkou.core.models.Verb;
import java.beans.ConstructorProperties;
import java.util.Objects;
import lombok.Builder;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(builderMethodName = "builder", toBuilder = true, setterPrefix = "with")
@With
@Description("Manage RBAC role bindings on Confluent Cloud.")
@JsonClassDescription("Manage RBAC role bindings on Confluent Cloud.")
@Names(singular = "ccloud-rb", plural = "ccloud-rbs", local = "role-bindings", shortNames = {"ccrb"})
@Verbs({
    Verb.APPLY,
    Verb.CREATE,
    Verb.DELETE,
    Verb.LIST
})
@JsonPropertyOrder({
    "apiVersion",
    "kind",
    "metadata",
    "spec"
})
@ApiVersion("iam.confluent.cloud/v1")
@Kind("RoleBinding")
@Jacksonized
@Reflectable
public class V1RoleBinding implements HasMetadata, HasSpec<V1RoleBindingSpec>, Resource {

    @JsonProperty("apiVersion")
    @Builder.Default
    private String apiVersion = "iam.confluent.cloud/v1";

    @JsonProperty("kind")
    @Builder.Default
    private String kind = "RoleBinding";

    @JsonProperty("metadata")
    private ObjectMeta metadata;

    @JsonProperty("spec")
    private V1RoleBindingSpec spec;

    public V1RoleBinding() {
    }

    @ConstructorProperties({
        "apiVersion",
        "kind",
        "metadata",
        "spec"
    })
    public V1RoleBinding(String apiVersion, String kind, ObjectMeta metadata, V1RoleBindingSpec spec) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
    }

    @JsonProperty("apiVersion")
    public String getApiVersion() {
        return apiVersion;
    }

    @JsonProperty("kind")
    public String getKind() {
        return kind;
    }

    @JsonProperty("metadata")
    public ObjectMeta getMetadata() {
        return metadata;
    }

    @JsonProperty("spec")
    public V1RoleBindingSpec getSpec() {
        return spec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V1RoleBinding rhs)) return false;
        return Objects.equals(apiVersion, rhs.apiVersion) &&
            Objects.equals(kind, rhs.kind) &&
            Objects.equals(metadata, rhs.metadata) &&
            Objects.equals(spec, rhs.spec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiVersion, kind, metadata, spec);
    }

    @Override
    public String toString() {
        return "V1RoleBinding[" +
            "apiVersion=" + apiVersion +
            ", kind=" + kind +
            ", metadata=" + metadata +
            ", spec=" + spec +
            ']';
    }
}
