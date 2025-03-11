/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import java.util.List;

@Reflectable
public record ValidatingResourcePolicySpec(
    @JsonPropertyDescription("Define how a policy evaluation error is handled")
    FailurePolicy failurePolicy,

    @JsonPropertyDescription("Match criteria for which resources the policy should validate")
    MatchResources matchResources,

    @JsonPropertyDescription("Rules to apply on matching resources")
    List<ResourcePolicyRule> rules
) {

    @Reflectable
    public record MatchResources(
        @JsonPropertyDescription("The resource selector")
        ResourceSelector resourceSelector
    ) {

    }

    @Reflectable
    public record ResourceSelector(
        @JsonPropertyDescription("Resource API versions to match")
        List<String> apiVersions,
        @JsonPropertyDescription("Resource Kinds to match")
        List<String> kinds
    ) {

    }

    @Reflectable
    public record ResourcePolicyRule(
        @JsonPropertyDescription("The rule name.")
        String name,
        @JsonPropertyDescription("The CEL expression to validate the resource.")
        String expression,
        @JsonPropertyDescription("The expression message")
        String messageExpression,
        @JsonPropertyDescription("The plain string message")
        String message
    ) {
    }
}
