/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.change;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.core.models.change.GenericResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChange;
import io.streamthoughts.jikkou.core.models.change.ResourceChangeSpec;
import io.streamthoughts.jikkou.core.models.change.StateChange;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.extension.confluent.MetadataAnnotations;
import io.streamthoughts.jikkou.extension.confluent.api.data.RoleBindingData;
import io.streamthoughts.jikkou.extension.confluent.models.V1RoleBinding;
import io.streamthoughts.jikkou.extension.confluent.models.V1RoleBindingSpec;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoleBindingChangeComputerTest {

    static final String TEST_PRINCIPAL = "User:sa-test";
    static final String TEST_ROLE = "CloudClusterAdmin";
    static final String TEST_CRN = "crn://confluent.cloud/organization=org-123";

    @Test
    void shouldReturnCreateChangeForNewRoleBinding() {
        RoleBindingChangeComputer computer = new RoleBindingChangeComputer(false);

        V1RoleBinding after = V1RoleBinding.builder()
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        List<ResourceChange> changes = computer.computeChanges(
            Collections.emptyList(),
            List.of(after)
        );

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.CREATE, changes.getFirst().getSpec().getOp());
    }

    @Test
    void shouldReturnNoneChangeForExistingRoleBinding() {
        RoleBindingChangeComputer computer = new RoleBindingChangeComputer(false);

        V1RoleBinding existing = V1RoleBinding.builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation(MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID, "rb-123")
                .build())
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        V1RoleBinding expected = V1RoleBinding.builder()
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        List<ResourceChange> changes = computer.computeChanges(
            List.of(existing),
            List.of(expected)
        );

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.NONE, changes.getFirst().getSpec().getOp());
    }

    @Test
    void shouldReturnDeleteChangeWhenOrphansDeleteEnabled() {
        RoleBindingChangeComputer computer = new RoleBindingChangeComputer(true);

        V1RoleBinding existing = V1RoleBinding.builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation(MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID, "rb-123")
                .build())
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        List<ResourceChange> changes = computer.computeChanges(
            List.of(existing),
            Collections.emptyList()
        );

        Assertions.assertEquals(1, changes.size());
        Assertions.assertEquals(Operation.DELETE, changes.getFirst().getSpec().getOp());
    }

    @Test
    void shouldNotReturnDeleteChangeWhenOrphansDeleteDisabled() {
        RoleBindingChangeComputer computer = new RoleBindingChangeComputer(false);

        V1RoleBinding existing = V1RoleBinding.builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation(MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID, "rb-123")
                .build())
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        List<ResourceChange> changes = computer.computeChanges(
            List.of(existing),
            Collections.emptyList()
        );

        Assertions.assertTrue(changes.isEmpty());
    }

    @Test
    void shouldReturnCorrectCreateChangeSpec() {
        RoleBindingChangeComputer computer = new RoleBindingChangeComputer(false);

        V1RoleBinding after = V1RoleBinding.builder()
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        List<ResourceChange> changes = computer.computeChanges(
            Collections.emptyList(),
            List.of(after)
        );

        ResourceChange expected = GenericResourceChange
            .builder(V1RoleBinding.class)
            .withSpec(ResourceChangeSpec
                .builder()
                .withOperation(Operation.CREATE)
                .withChange(StateChange.create(
                    "entry",
                    new RoleBindingData(TEST_PRINCIPAL, TEST_ROLE, TEST_CRN))
                )
                .build()
            )
            .build();

        Assertions.assertEquals(List.of(expected), changes);
    }
}
