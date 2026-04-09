/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.extension.confluent.adapter;

import io.jikkou.core.models.ObjectMeta;
import io.jikkou.extension.confluent.MetadataAnnotations;
import io.jikkou.extension.confluent.api.data.RoleBindingData;
import io.jikkou.extension.confluent.models.V1RoleBinding;
import io.jikkou.extension.confluent.models.V1RoleBindingSpec;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RoleBindingAdapterTest {

    static final String TEST_ID = "rb-abc123";
    static final String TEST_PRINCIPAL = "User:sa-test";
    static final String TEST_ROLE = "CloudClusterAdmin";
    static final String TEST_CRN = "crn://confluent.cloud/organization=org-123/environment=env-456";

    @Test
    void shouldMapResourceToData() {
        V1RoleBinding resource = V1RoleBinding.builder()
            .withMetadata(ObjectMeta.builder()
                .withAnnotation(MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID, TEST_ID)
                .build())
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        RoleBindingData result = RoleBindingAdapter.map(resource);

        Assertions.assertEquals(TEST_ID, result.id());
        Assertions.assertEquals(TEST_PRINCIPAL, result.principal());
        Assertions.assertEquals(TEST_ROLE, result.roleName());
        Assertions.assertEquals(TEST_CRN, result.crnPattern());
    }

    @Test
    void shouldMapResourceToDataWithoutId() {
        V1RoleBinding resource = V1RoleBinding.builder()
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(TEST_PRINCIPAL)
                .withRoleName(TEST_ROLE)
                .withCrnPattern(TEST_CRN)
                .build())
            .build();

        RoleBindingData result = RoleBindingAdapter.map(resource);

        Assertions.assertNull(result.id());
        Assertions.assertEquals(TEST_PRINCIPAL, result.principal());
    }

    @Test
    void shouldMapDataToResource() {
        RoleBindingData data = new RoleBindingData(TEST_ID, TEST_PRINCIPAL, TEST_ROLE, TEST_CRN);

        V1RoleBinding result = RoleBindingAdapter.map(data);

        Assertions.assertEquals(TEST_PRINCIPAL, result.getSpec().getPrincipal());
        Assertions.assertEquals(TEST_ROLE, result.getSpec().getRoleName());
        Assertions.assertEquals(TEST_CRN, result.getSpec().getCrnPattern());
        Assertions.assertEquals(TEST_ID,
            result.getMetadata().findAnnotationByKey(MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID).orElse(null));
    }

    @Test
    void shouldMapDataWithoutIdToResource() {
        RoleBindingData data = new RoleBindingData(TEST_PRINCIPAL, TEST_ROLE, TEST_CRN);

        V1RoleBinding result = RoleBindingAdapter.map(data);

        Assertions.assertEquals(TEST_PRINCIPAL, result.getSpec().getPrincipal());
        Assertions.assertTrue(
            result.getMetadata().findAnnotationByKey(MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID).isEmpty());
    }

    @Test
    void shouldMapListOfDataToResources() {
        List<RoleBindingData> entries = List.of(
            new RoleBindingData(TEST_ID, TEST_PRINCIPAL, TEST_ROLE, TEST_CRN),
            new RoleBindingData("rb-xyz789", "User:sa-other", "DeveloperRead", TEST_CRN)
        );

        List<V1RoleBinding> result = RoleBindingAdapter.map(entries);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(TEST_PRINCIPAL, result.get(0).getSpec().getPrincipal());
        Assertions.assertEquals("User:sa-other", result.get(1).getSpec().getPrincipal());
    }

    @Test
    void shouldMapNullToNull() {
        Assertions.assertNull(RoleBindingAdapter.map((V1RoleBinding) null));
        Assertions.assertNull(RoleBindingAdapter.map((RoleBindingData) null));
    }
}
