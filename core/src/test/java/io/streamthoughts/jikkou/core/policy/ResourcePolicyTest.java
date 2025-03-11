/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import io.streamthoughts.jikkou.core.policy.model.FailurePolicy;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicy;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec.MatchResources;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec.ResourcePolicyRule;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourcePolicyTest {

    private static final GenericResource GENERIC_RESOURCE = new GenericResource(
        "io.jikkou/v1",
        "Test",
        new ObjectMeta("TestResource"),
        null,
        null

    );

    @Test
    void shouldReturnEmptyRuleWhenEvaluatingGivenValidResource() {
        // Given
        ValidatingResourcePolicy resource = new ValidatingResourcePolicy.Builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName("TestPolicy")
                .build()
            )
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    new MatchResources(null),
                    List.of(
                        new ResourcePolicyRule(
                            "test",
                            "resource.metadata.name == 'TestResource'",
                            "'Invalid name: ' + resource.metadata.name",
                            null
                        )
                    )
                )
            )
            .build();
        // When
        ResourcePolicy policy = new ResourcePolicy(resource);

        // Then
        ResourcePolicyResult result = policy.evaluate(GENERIC_RESOURCE);
        Assertions.assertEquals(
            new ResourcePolicyResult(
                "TestPolicy",
                FailurePolicy.FAIL,
                List.of()
            ),
            result
        );
    }


    @Test
    void shouldReturnFailedRuleWhenEvaluatingGivenInvalidResource() {
        // Given
        ValidatingResourcePolicy resource = new ValidatingResourcePolicy.Builder()
            .withMetadata(ObjectMeta
                .builder()
                .withName("TestPolicy")
                .build()
            )
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    new MatchResources(null),
                    List.of(
                        new ResourcePolicyRule(
                            "test",
                            "resource.metadata.name == '???'",
                            "'Invalid name: ' + resource.metadata.name",
                            null
                        )
                    )
                )
            )
            .build();
        // When
        ResourcePolicy policy = new ResourcePolicy(resource);

        // Then
        ResourcePolicyResult result = policy.evaluate(GENERIC_RESOURCE);
        Assertions.assertEquals(
            new ResourcePolicyResult(
                "TestPolicy",
                FailurePolicy.FAIL,
                List.of(new ResourcePolicyResult.RuleFailure("test", "Invalid name: TestResource"))
            ),
            result
        );
    }
}