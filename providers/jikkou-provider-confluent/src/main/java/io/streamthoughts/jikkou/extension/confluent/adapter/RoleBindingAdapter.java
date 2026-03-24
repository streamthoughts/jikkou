/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.adapter;

import static io.streamthoughts.jikkou.extension.confluent.MetadataAnnotations.CONFLUENT_CLOUD_ROLE_BINDING_ID;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.extension.confluent.api.data.RoleBindingData;
import io.streamthoughts.jikkou.extension.confluent.models.V1RoleBinding;
import io.streamthoughts.jikkou.extension.confluent.models.V1RoleBindingSpec;
import java.util.List;

public final class RoleBindingAdapter {

    /**
     * Maps a {@link V1RoleBinding} resource to a {@link RoleBindingData} DTO.
     * The ID is extracted from the metadata annotation if present.
     */
    public static RoleBindingData map(final V1RoleBinding resource) {
        if (resource == null) return null;
        V1RoleBindingSpec spec = resource.getSpec();
        String id = resource.optionalMetadata()
            .flatMap(meta -> meta.findAnnotationByKey(CONFLUENT_CLOUD_ROLE_BINDING_ID))
            .map(Object::toString)
            .orElse(null);
        return new RoleBindingData(
            id,
            spec.getPrincipal(),
            spec.getRoleName(),
            spec.getCrnPattern()
        );
    }

    /**
     * Maps a list of {@link RoleBindingData} DTOs to a list of {@link V1RoleBinding} resources.
     */
    public static List<V1RoleBinding> map(final List<RoleBindingData> entries) {
        return entries.stream()
            .map(RoleBindingAdapter::map)
            .toList();
    }

    /**
     * Maps a {@link RoleBindingData} DTO to a {@link V1RoleBinding} resource.
     * The ID is stored in a metadata annotation.
     */
    public static V1RoleBinding map(final RoleBindingData data) {
        if (data == null) return null;
        ObjectMeta.ObjectMetaBuilder objectMetaBuilder = ObjectMeta.builder();
        if (data.id() != null) {
            objectMetaBuilder = objectMetaBuilder
                .withAnnotation(CONFLUENT_CLOUD_ROLE_BINDING_ID, data.id());
        }
        return V1RoleBinding.builder()
            .withMetadata(objectMetaBuilder.build())
            .withSpec(V1RoleBindingSpec.builder()
                .withPrincipal(data.principal())
                .withRoleName(data.roleName())
                .withCrnPattern(data.crnPattern())
                .build()
            )
            .build();
    }
}
