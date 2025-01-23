/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.selector.ExpressionOperator;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import java.util.List;

@Reflectable
public record ValidatingResourcePolicySpec(
    @JsonPropertyDescription("Define how a policy evaluation error is handled")
    FailurePolicy failurePolicy,

    @JsonPropertyDescription("The filtering criteria for selecting resources.")
    ResourceSelector selector,

    @JsonPropertyDescription("The rules to apply on matching resources")
    List<ResourcePolicyRule> rules
) {

    @Reflectable
    public record ResourceSelector(
        @JsonPropertyDescription("The strategy for matching resources when multiple selectors are used.")
        SelectorMatchingStrategy matchingStrategy,
        @JsonPropertyDescription("The resource-based selectors")
        List<MatchResource> matchResources,
        @JsonPropertyDescription("The label-based selectors")
        List<MatchLabel> matchLabels,
        @JsonPropertyDescription("The CEL-based selectors")
        List<String> matchExpressions
    ) {

    }

    @Reflectable
    public record MatchLabel(
        @JsonPropertyDescription("The label key to match.")
        String key,
        @JsonPropertyDescription("The logical condition applied to the label's value.")
        ExpressionOperator operator,
        @JsonPropertyDescription("A list of values used for comparison.")
        List<String> values
    ) {

    }

    @Reflectable
    public record MatchResource(
        @JsonPropertyDescription("Resource API versions to match")
        String apiVersion,
        @JsonPropertyDescription("Resource Kinds to match")
        String kind
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
