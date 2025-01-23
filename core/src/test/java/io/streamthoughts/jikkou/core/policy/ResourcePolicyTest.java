/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.policy;

import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.generics.GenericResource;
import io.streamthoughts.jikkou.core.policy.model.FailurePolicy;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicy;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec.ResourcePolicyRule;
import io.streamthoughts.jikkou.core.policy.model.ValidatingResourcePolicySpec.ResourceSelector;
import io.streamthoughts.jikkou.core.selector.SelectorMatchingStrategy;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResourcePolicyTest {

    private static final GenericResource GENERIC_RESOURCE = new GenericResource(
        "io.jikkou/v1",
        "Test",
        new ObjectMeta("TestResource"),
        null,
        null

    );

    private static final ObjectMeta TEST_OBJECT_META = ObjectMeta
        .builder()
        .withName("TestPolicy")
        .build();

    @Test
    void shouldAcceptResourceGivenMatchingApiVersion() {
        // Given
        ValidatingResourcePolicy resource = new ValidatingResourcePolicy.Builder()
            .withMetadata(TEST_OBJECT_META
            )
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    new ResourceSelector(
                        SelectorMatchingStrategy.ALL,
                        List.of(new ValidatingResourcePolicySpec.MatchResource("v1", null)),
                        null,
                        null
                    ),
                    List.of()
                )
            )
            .build();

        ResourcePolicy policy = new ResourcePolicy(resource);

        // When
        HasMetadata mkResource = Mockito.mock(HasMetadata.class);
        Mockito.when(mkResource.getApiVersion()).thenReturn("v1");
        Mockito.when(mkResource.getKind()).thenReturn("Mock");

        boolean accept = policy.canAccept(mkResource);
        // Then
        Assertions.assertTrue(accept);
    }

    @Test
    void shouldAcceptResourceGivenMatchingKind() {
        // Given
        ValidatingResourcePolicy resource = new ValidatingResourcePolicy.Builder()
            .withMetadata(TEST_OBJECT_META)
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    new ResourceSelector(
                        SelectorMatchingStrategy.ALL,
                        List.of(new ValidatingResourcePolicySpec.MatchResource(null, "Mock")),
                        null,
                        null
                    ),
                    List.of()
                )
            )
            .build();

        ResourcePolicy policy = new ResourcePolicy(resource);

        // When
        HasMetadata mkResource = Mockito.mock(HasMetadata.class);
        Mockito.when(mkResource.getApiVersion()).thenReturn("v1");
        Mockito.when(mkResource.getKind()).thenReturn("Mock");

        boolean accept = policy.canAccept(mkResource);
        // Then
        Assertions.assertTrue(accept);
    }

    @Test
    void shouldNotAcceptResourceGivenMatchingKind() {
        // Given
        ValidatingResourcePolicy resource = new ValidatingResourcePolicy.Builder()
            .withMetadata(TEST_OBJECT_META)
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    new ResourceSelector(
                        SelectorMatchingStrategy.ALL,
                        List.of(new ValidatingResourcePolicySpec.MatchResource(null, "Mock")),
                        null,
                        null
                    ),
                    List.of()
                )
            )
            .build();

        ResourcePolicy policy = new ResourcePolicy(resource);

        // When
        HasMetadata mkResource = Mockito.mock(HasMetadata.class);
        Mockito.when(mkResource.getApiVersion()).thenReturn("v1");
        Mockito.when(mkResource.getKind()).thenReturn("Any");

        boolean accept = policy.canAccept(mkResource);

        // Then
        Assertions.assertFalse(accept);
    }

    @Test
    void shouldReturnEmptyRuleWhenEvaluatingGivenValidResource() {
        // Given
        ValidatingResourcePolicy resource = new ValidatingResourcePolicy.Builder()
            .withMetadata(TEST_OBJECT_META
            )
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    null,
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
            .withMetadata(TEST_OBJECT_META
            )
            .withSpec(
                new ValidatingResourcePolicySpec(
                    FailurePolicy.FAIL,
                    null,
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