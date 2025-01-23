/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.Resource;
import io.streamthoughts.jikkou.core.policy.ResourcePolicyResult.RuleFailure;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicy;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec;
import io.streamthoughts.jikkou.core.selector.AggregateSelector;
import io.streamthoughts.jikkou.core.selector.ExpressionSelector;
import io.streamthoughts.jikkou.core.selector.LabelSelector;
import io.streamthoughts.jikkou.core.selector.PreparedExpression;
import io.streamthoughts.jikkou.core.selector.ResourceSelector;
import io.streamthoughts.jikkou.core.selector.Selector;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a executable resource policy.
 *
 * @see ValidatingResourcePolicy
 */
public final class ResourcePolicy {

    private final ValidatingResourcePolicy policy;

    private final List<CompiledRule> rules;

    /**
     * Creates a new {@link ResourcePolicy} instance.
     *
     * @param policy The {@link ValidatingResourcePolicy}.
     */
    public ResourcePolicy(final ValidatingResourcePolicy policy) {
        this.policy = Objects.requireNonNull(policy);
        this.rules = policy.getSpec().rules()
            .stream().map(rule -> {
                CelExpression<Boolean> expression = CelExpressionFactory.bool().compile(rule.expression());
                CelExpression<String> message = rule.message() != null ?
                    resource -> rule.message() :
                    CelExpressionFactory.string().compile(rule.messageExpression());
                return new CompiledRule(rule.name(), expression, message);
            }).toList();
    }

    /**
     * Checks whether this policy accepts the given resource.
     *
     * @param resource The resource on which to apply the policy.
     * @return {@code true} if the policy can be applied. Otherwise {@code false}.
     */
    public boolean canAccept(final HasMetadata resource) {
        ValidatingResourcePolicySpec.ResourceSelector selector = this.policy.getSpec().selector();

        if (selector == null) {
            return false;
        }

        List<Selector> selectors = new ArrayList<>();
        selectors.addAll(Optional.ofNullable(selector.matchResources())
            .stream()
            .flatMap(Collection::stream)
            .map(it -> new ResourceSelector(it.apiVersion(), it.kind()))
            .toList()
        );

        selectors.addAll(Optional.ofNullable(selector.matchLabels())
            .stream()
            .flatMap(Collection::stream)
            .map(it -> new LabelSelector(new PreparedExpression(null, it.key(), it.operator(), it.values())))
            .toList()
        );

        selectors.addAll(Optional.ofNullable(selector.matchExpressions())
            .stream()
            .flatMap(Collection::stream)
            .map(ExpressionSelector::new)
            .toList()
        );

        SelectorMatchingStrategy matchingStrategy = Optional
            .ofNullable(selector.matchingStrategy()).
            orElse(SelectorMatchingStrategy.ALL);

        return new AggregateSelector(selectors, matchingStrategy).apply(resource);
    }

    /**
     * Evaluates this policy on the given resource.
     *
     * @param resource The resource on which to apply the policy.
     * @return the {@link ResourcePolicyResult}.
     */
    public ResourcePolicyResult evaluate(final Resource resource) {
        List<RuleFailure> failures =
            rules.stream()
                .flatMap(rule -> rule.eval(resource).stream())
                .toList();

        return new ResourcePolicyResult(
            policy.getMetadata().getName(),
            policy.getSpec().failurePolicy(),
            failures
        );
    }

    private record CompiledRule(String name,
                                CelExpression<Boolean> expression,
                                CelExpression<String> message) {

        Optional<RuleFailure> eval(final Resource resource) {
            if (!expression().eval(resource)) {
                return Optional.of(new RuleFailure(name(), message().eval(resource)));
            }
            return Optional.empty();
        }
    }
}
