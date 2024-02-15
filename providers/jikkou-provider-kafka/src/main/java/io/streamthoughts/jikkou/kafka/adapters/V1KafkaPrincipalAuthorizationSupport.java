/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.core.models.ObjectMeta;
import io.streamthoughts.jikkou.kafka.model.KafkaAclBinding;
import io.streamthoughts.jikkou.kafka.model.KafkaAclResource;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAcl;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorization;
import io.streamthoughts.jikkou.kafka.models.V1KafkaPrincipalAuthorizationSpec;
import io.streamthoughts.jikkou.kafka.models.V1KafkaResourceMatcher;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.kafka.common.acl.AclOperation;

public final class V1KafkaPrincipalAuthorizationSupport {

    public static Stream<V1KafkaPrincipalAuthorization> from(final Collection<KafkaAclBinding> bindings) {

        return bindings
                .stream()
                .collect(Collectors.groupingBy(KafkaAclBinding::principal))
                .entrySet()
                .stream()
                .map(e -> from(e.getKey(), e.getValue()));
    }

    private static V1KafkaPrincipalAuthorization from(final String principal,
                                            final List<KafkaAclBinding> bindings) {

        List<V1KafkaPrincipalAcl> acl = bindings
                .stream()
                .collect(Collectors.groupingBy(KafkaAclResource::new))
                .entrySet()
                .stream()
                .flatMap(resourceAndBindings -> {
                    return resourceAndBindings.getValue()
                            .stream()
                            .collect(Collectors.groupingBy(KafkaAclBinding::type))
                            .entrySet()
                            .stream()
                            .map(typeAndBindings -> {
                                final List<AclOperation> operations = typeAndBindings.getValue()
                                        .stream()
                                        .map(KafkaAclBinding::operation)
                                        .toList();

                                var pattern = resourceAndBindings.getKey();
                                return V1KafkaPrincipalAcl.builder()
                                        .withResource(V1KafkaResourceMatcher
                                                .builder()
                                                .withType(pattern.resourceType())
                                                .withPattern(pattern.pattern())
                                                .withPatternType(pattern.patternType())
                                                .build()
                                        )
                                        .withType(typeAndBindings.getKey())
                                        .withOperations(operations)
                                        .withHost(pattern.host())
                                        .build();

                            });
                }).toList();

        return V1KafkaPrincipalAuthorization.builder()
                .withMetadata(ObjectMeta
                        .builder()
                        .withName(principal)
                        .build()
                )
                .withSpec(V1KafkaPrincipalAuthorizationSpec
                        .builder()
                        .withAcls(acl)
                        .build()
                )
                .build();
    }
}
