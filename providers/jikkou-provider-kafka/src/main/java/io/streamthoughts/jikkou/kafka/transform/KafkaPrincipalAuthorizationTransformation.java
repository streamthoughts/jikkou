/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.transform;

import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.annotation.Enabled;
import io.streamthoughts.jikkou.core.annotation.Priority;
import io.streamthoughts.jikkou.core.annotation.SupportedResource;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasPriority;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.transform.Transformation;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * Transformation to apply all roles to topic objects.
 */
@SupportedResource(type = V1KafkaPrincipalAuthorization.class)
@Enabled
@Priority(HasPriority.HIGHEST_PRECEDENCE)
public class KafkaPrincipalAuthorizationTransformation implements Transformation<V1KafkaPrincipalAuthorization> {

    /**
     * {@inheritDoc
     */
    @Override
    public @NotNull Optional<V1KafkaPrincipalAuthorization> transform(@NotNull V1KafkaPrincipalAuthorization toTransform,
                                                                      @NotNull HasItems items,
                                                                      @NotNull ReconciliationContext context) {
        Set<String> roles = toTransform.getSpec().getRoles();
        if (roles == null || roles.isEmpty()) {
            return Optional.of(toTransform);
        }

        HasItems definedRoleResources = ResourceList.of(items.getAllByKind(V1KafkaPrincipalRole.class));
        definedRoleResources.verifyNoDuplicateMetadataName();

        List<V1KafkaPrincipalAcl> aclBindingsFromRoles = roles
                .stream()
                .map(roleName -> definedRoleResources.getByName(roleName, V1KafkaPrincipalRole.class))
                .flatMap(r -> r.getSpec().getAcls().stream())
                .toList();

        List<V1KafkaPrincipalAcl> aclBindings = new ArrayList<>(toTransform.getSpec().getAcls());
        aclBindings.addAll(aclBindingsFromRoles);

        V1KafkaPrincipalAuthorization result = toTransform.toBuilder()
                .withSpec(toTransform.getSpec().toBuilder()
                        .withAcls(aclBindings)
                        .clearRoles()
                        .build()
                ).build();

        return Optional.of(result);
    }
}
