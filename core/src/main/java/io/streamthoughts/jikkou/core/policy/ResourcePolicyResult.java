/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import io.streamthoughts.jikkou.core.annotation.Reflectable;
import io.streamthoughts.jikkou.core.policy.model.FailurePolicy;
import java.util.List;

@Reflectable
public record ResourcePolicyResult(
    String policyName,
    FailurePolicy failurePolicy,
    List<RuleFailure> rules
) {
    public boolean hasErrors() {
        return rules != null && !rules.isEmpty();
    }

    @Reflectable
    public record RuleFailure(
        String name,
        String errorMessage
    ) {}
}
