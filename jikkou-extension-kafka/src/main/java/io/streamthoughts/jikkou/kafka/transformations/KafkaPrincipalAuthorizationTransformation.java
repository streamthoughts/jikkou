/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.kafka.transformations;

import io.streamthoughts.jikkou.api.annotations.ExtensionEnabled;
import io.streamthoughts.jikkou.api.annotations.SupportedResource;
import io.streamthoughts.jikkou.api.model.GenericResourceListObject;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.transform.ResourceTransformation;
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
@ExtensionEnabled
public class KafkaPrincipalAuthorizationTransformation implements ResourceTransformation<V1KafkaPrincipalAuthorization> {

    /**
     * {@inheritDoc
     */
    @Override
    public @NotNull Optional<V1KafkaPrincipalAuthorization> transform(@NotNull V1KafkaPrincipalAuthorization toTransform,
                                                                      @NotNull HasItems items) {
        Set<String> roles = toTransform.getSpec().getRoles();
        if (roles == null || roles.isEmpty()) {
            return Optional.of(toTransform);
        }

        HasItems definedRoleResources = new GenericResourceListObject(items.getAllByKind(V1KafkaPrincipalRole.class));
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
