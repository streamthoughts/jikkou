/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.kafka.adapters;

import io.streamthoughts.jikkou.api.model.ObjectMeta;
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
                .collect(Collectors.groupingBy(KafkaAclBinding::getPrincipal))
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
                            .collect(Collectors.groupingBy(KafkaAclBinding::getType))
                            .entrySet()
                            .stream()
                            .map(typeAndBindings -> {
                                final List<AclOperation> operations = typeAndBindings.getValue()
                                        .stream()
                                        .map(KafkaAclBinding::getOperation)
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

        return new V1KafkaPrincipalAuthorization().toBuilder()
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
